#define OMPI_SKIP_MPICXX 1

#include <algorithm>
#include <iostream>
#include <mpi.h>
#include <vector>
#include <queue>
#include <set>
#include <string>
#include <unordered_map>

#include "platform_load_time_gen.hpp"

using std::string;
using std::unordered_map;
using std::vector;
using adjacency_matrix = std::vector<std::vector<size_t>>;

const char colors[] = {'g', 'y', 'b'};

struct Train {
    int line; // 0 = green, 1 == yellow, 2 == blue
    int id;
    int is_forward;
    int arrival_tick;
    int status; // 0 means in holding, 1 means loading, 2 means travelling.
    
    Train() : 
        line(-1), 
        id(-1),
        is_forward(-1),
        arrival_tick(-1),
        status(-1) {}

    Train(int line, int id, int is_forward, int arrival_tick) : 
        line(line), 
        id(id),
        is_forward(is_forward),
        arrival_tick(arrival_tick),
        status(0) {}
};

struct TrainOrder {
    bool operator() (const Train* t1, Train* t2) {
        if (t1->arrival_tick == t2->arrival_tick) {
            return t1->id > t2->id;
        }
        return t1->arrival_tick > t2->arrival_tick;
    } 
};

struct Link {
    bool is_valid;
    int dist;
    string src;
    string dst;
    PlatformLoadTimeGen pltg;
    bool is_train_travelling;
    bool is_train_loading;
    int time_finished_loading;
    int time_finished_travelling;
    Train* curr_train;
    Train* travelling_train;

    std::priority_queue<Train*, std::vector<Train*>, TrainOrder> holding_platform; // holding platform for link

    Link() : 
        is_valid(false),
        dist(0), 
        src(""), 
        dst(""),
        pltg(1),
        is_train_travelling(false),
        is_train_loading(false),
        time_finished_loading(0), 
        time_finished_travelling(0) {}

    Link(int dist, string src, string dst, int popularity) : 
        is_valid(true),
        dist(dist), 
        src(src), 
        dst(dst),
        pltg(popularity),
        is_train_travelling(false),
        is_train_loading(false),
        time_finished_loading(0), 
        time_finished_travelling(0) {}

    void load_next_train() {
        if (!holding_platform.empty()) {
            curr_train = holding_platform.top();
            holding_platform.pop();
            is_train_loading = true;
        }
    }

    void set_loading_time(int loading_time) {
        time_finished_loading = loading_time;
    }
};

struct Station {
    string platform_name;
    const size_t popularity;

    std::vector<Link*> outgoing_links;
    std::unordered_map<string, Link> unique_links;
    std::unordered_map<int, Link*> forward_color_to_links;
    std::unordered_map<int, Link*> backward_color_to_links;
    std::unordered_map<int, Train> trains_in_charge;

    Station(string platform_name, const size_t popularity) : 
        platform_name(platform_name), 
        popularity(popularity) {}

    void add_outgoing_link(int line, string dst, int dist, int direction) {
        if (direction == 1) {
            if (!unique_links.count(dst)) {
                unique_links[dst] = Link(dist, platform_name, dst, popularity);
                outgoing_links.push_back(&unique_links[dst]);
            }
            forward_color_to_links[line] = &unique_links[dst];
        } else {
            if (!unique_links.count(dst)) {
                unique_links[dst] = Link(dist, platform_name, dst, popularity);
                outgoing_links.push_back(&unique_links[dst]);
            }
            backward_color_to_links[line] = &unique_links[dst];
        }
    }

    void spawn_train(int line, int id, int is_forward, int tick) {
        trains_in_charge[id] = Train(line, id, is_forward, tick);
        if (is_forward == 1) {
            Link* link = forward_color_to_links[line];
            link->holding_platform.push(&trains_in_charge[id]);
        } else {
            Link* link = backward_color_to_links[line];
            link->holding_platform.push(&trains_in_charge[id]);
        }
    }
};

string format_string(char line, int train_id, string action) {
    string msg =  line + std::to_string(train_id) + '-' + action;
    return msg;
}

void simulate(size_t num_stations, const vector<string> &station_names, const std::vector<size_t> &popularities,
              const adjacency_matrix &mat, const unordered_map<char, vector<string>> &station_lines, size_t ticks,
              const unordered_map<char, size_t> num_trains, size_t num_ticks_to_print, size_t mpi_rank,
              size_t total_processes) {
    std::unordered_map<string, int> station_to_id_map; // station name -> id 
    for (size_t i = 0; i < num_stations; i++) {
        station_to_id_map[station_names[i]] = i;
    }

    std::unordered_map<int, std::vector<int>> rank_to_station; // rank -> station_ids
    std::unordered_map<int, int> station_to_rank; // station_ids -> rank
    for (size_t i = 0; i < num_stations; i++) {
        int assigned_rank = i % total_processes;
        rank_to_station[assigned_rank].push_back(i);
        station_to_rank[i] = assigned_rank;
    } 

    // Initialise stations within this rank only
    std::vector<Station> station_vector; // stations within this rank
    for (int station_id : rank_to_station[mpi_rank]) {
        station_vector.push_back(Station(station_names[station_id], popularities[station_id]));
    }

    std::vector<std::vector<int>> terminal_stations(3, std::vector<int>(2)); // terminal station_ids for each line
    std::vector<std::vector<int>> terminal_spawn_count(3, std::vector<int>(2)); // number of trains to spawn at each terminal station for each line
    int total_trains_count = 0;

    for (int i = 0; i < 3; i++) {
        total_trains_count += num_trains.at(colors[i]);

        // storing terminal stations and number of trains to spawn at each terminal station
        string starting_station = station_lines.at(colors[i])[0];
        terminal_stations[i][0] = station_to_id_map[starting_station];
        string ending_station = station_lines.at(colors[i]).back();
        terminal_stations[i][1] = station_to_id_map[ending_station];

        int trains_to_spawn_at_end = num_trains.at(colors[i]) / 2; // this will always take the lower of the half; 4 when total is 9
        int trains_to_spawn_at_start = num_trains.at(colors[i]) - trains_to_spawn_at_end;
        terminal_spawn_count[i][0] = trains_to_spawn_at_start;
        terminal_spawn_count[i][1] = trains_to_spawn_at_end;
    }

    for (Station& station: station_vector) {
        for (int i = 0; i < 3; i++) {
            for (size_t j = 0; j < station_lines.at(colors[i]).size() - 1; j++) {
                // add links in the forward direction
                string curr_station = station_lines.at(colors[i])[j];
                if (curr_station != station.platform_name) {
                    continue;
                }
                string next_station = station_lines.at(colors[i])[j + 1];
                int curr_station_id = station_to_id_map[station.platform_name];
                int next_station_id = station_to_id_map[next_station];
                station.add_outgoing_link(i, next_station, mat[curr_station_id][next_station_id], 1);
            }
            
            for (int j = station_lines.at(colors[i]).size() - 1; j >= 1; j--) {
                // add links in the backward direction
                string curr_station = station_lines.at(colors[i])[j];
                if (curr_station != station.platform_name) {
                    continue;
                }
                string next_station = station_lines.at(colors[i])[j - 1];
                int curr_station_id = station_to_id_map[station.platform_name];
                int next_station_id = station_to_id_map[next_station];
                station.add_outgoing_link(i, next_station, mat[curr_station_id][next_station_id], -1);
            }
        }
    }

    int trains_count = 0;
    int tick_to_start_printing = ticks - num_ticks_to_print;
    for (size_t i = 0; i < ticks; i++) {
        std::vector<string> msg; 
        int tick = i;
        MPI_Bcast(&tick, 1, MPI_INT, 0, MPI_COMM_WORLD);
        int total_spawned_trains_count = 0;
        // Spawn trains
        if (trains_count < total_trains_count) {
            for (int j = 0; j < 3; j++) {
                int spawned_train_count = 0;
                for (Station& curr_station : station_vector) {
                    if (station_to_id_map[curr_station.platform_name] == terminal_stations[j][0] && terminal_spawn_count[j][0] > 0) {
                        curr_station.spawn_train(j, trains_count, 1, tick);
                        terminal_spawn_count[j][0]--;
                        spawned_train_count++;
                    }

                    if (station_to_id_map[curr_station.platform_name] == terminal_stations[j][1] && terminal_spawn_count[j][1] > 0) {
                        curr_station.spawn_train(j, trains_count + 1, -1, tick);
                        terminal_spawn_count[j][1]--;
                        spawned_train_count++;
                    }   
                } 
                MPI_Allreduce(&spawned_train_count, &total_spawned_trains_count, 1, MPI_INT, MPI_SUM, MPI_COMM_WORLD);
                trains_count += total_spawned_trains_count;
            }
        }
        
        MPI_Barrier(MPI_COMM_WORLD);
        //////////////////////////////////// PHASE 2 ////////////////////////////////////
        // each link sends train to next station if possible. If there is no train at platform, poll from holding platform
        for (Station& curr_station : station_vector) {
            int number_of_links = curr_station.outgoing_links.size();
            for (int j = 0; j < number_of_links; j++) {
                Link* link = curr_station.outgoing_links[j];
                if (link == nullptr || !link->is_valid) {
                    continue;
                }
                int dst_station_number = station_to_id_map[link->dst];
                int dst_rank = station_to_rank[dst_station_number];
                MPI_Request request;
                if (link->is_train_travelling && tick >= link->time_finished_travelling) {
                    link->is_train_travelling = false;
                    // send msg to next station
                    int data[3] = {link->travelling_train->line, link->travelling_train->id, link->travelling_train->is_forward};
                    MPI_Isend(data, 3, MPI_INT, dst_rank, dst_station_number, MPI_COMM_WORLD, &request);
                    link->travelling_train->status = 0;
                    curr_station.trains_in_charge.erase(link->travelling_train->id);
                } else {
                    int data[3] = {-1,-1,-1};
                    MPI_Isend(data, 3, MPI_INT, dst_rank, dst_station_number, MPI_COMM_WORLD, &request);
                }
                if (!link->is_train_travelling) {
                    if (link->is_train_loading && tick >= link->time_finished_loading) {
                        link->is_train_loading = false;
                        link->is_train_travelling = true;
                        link->travelling_train = link->curr_train;
                        link->time_finished_travelling = tick + link->dist;
                        link->travelling_train->status = 2;
                        link->curr_train = nullptr;
                    }
                }
            }
        }

        for (Station& curr_station : station_vector) {
            int number_of_links = curr_station.outgoing_links.size();
            int curr_station_number = station_to_id_map[curr_station.platform_name];
            // collate all msg from other stations
            for (int k = 0; k < number_of_links; k++) {
                int recv_data[3];
                MPI_Status status; 
                MPI_Recv(recv_data, 3, MPI_INT, MPI_ANY_SOURCE, curr_station_number, MPI_COMM_WORLD, &status);
                if (recv_data[0] != -1) {
                    int direction = recv_data[2];
                    if (direction == -1 && curr_station_number == terminal_stations[recv_data[0]][0]) {
                        // turn around
                        direction = 1;
                    } else if (direction == 1 && curr_station_number == terminal_stations[recv_data[0]][1]) {
                        // turn around
                        direction = -1;
                    }
                    curr_station.spawn_train(recv_data[0], recv_data[1], direction, tick);
                }
            }
            for (int j = 0; j < number_of_links; j++) {
                Link* link = curr_station.outgoing_links[j];
                if (!link->is_train_loading) {
                    link->load_next_train();
                    if (link->is_train_loading) {
                        int wait_time = link->pltg.next(link->curr_train->id);
                        link->curr_train->status = 1;
                        link->set_loading_time(tick + wait_time);
                    }
                }
            }
        }
        MPI_Barrier(MPI_COMM_WORLD);

        if (tick < tick_to_start_printing) {
            continue;
        }

        //////////////////////////////////// FINAL PHASE ////////////////////////////////////
        // Code referred from https://stackoverflow.com/questions/31890523/how-to-use-mpi-gatherv-for-collecting-strings-of-diiferent-length-from-different

        for (Station& curr_station : station_vector) {
            for (const auto& [id, train]: curr_station.trains_in_charge) {
                if (train.status == 0) {
                    msg.push_back(format_string(colors[train.line], train.id, curr_station.platform_name + "#"));
                } else if (train.status == 1) {
                    msg.push_back(format_string(colors[train.line], train.id, curr_station.platform_name + "%"));
                } else {
                    if (train.is_forward == 1) {
                        string dst = curr_station.forward_color_to_links[train.line]->dst;
                        msg.push_back(format_string(colors[train.line], train.id, curr_station.platform_name + "->" + dst));
                    } else {
                        string dst = curr_station.backward_color_to_links[train.line]->dst;
                        msg.push_back(format_string(colors[train.line], train.id, curr_station.platform_name + "->" + dst));
                    }
                    
                }
            }        
        }

        // gathering all vectors to rank 0 for printing
        std::vector<int> local_msg_size; // size of each string in msg 
        std::string combined_msg; // all concated together
        for (string& str : msg) {
            local_msg_size.push_back(str.size());
            combined_msg += str;  
        }
        int local_concat_size = combined_msg.size();

        std::vector<int> size_of_fragments(total_processes); // size of each fragment across all ranks
        MPI_Gather(&local_concat_size, 1, MPI_INT, size_of_fragments.data(), 1, MPI_INT, 0, MPI_COMM_WORLD);

        std::vector<char> all_fragments; // combined chars of all msgs
        std::vector<int> indexes(total_processes, 0); // indexes across all ranks
        if (mpi_rank == 0) {
            int total_size = std::accumulate(size_of_fragments.begin(), size_of_fragments.end(), 0);
            all_fragments.resize(total_size);

            for (size_t i = 1; i < total_processes; i++) {
                indexes[i] = indexes[i - 1] + size_of_fragments[i - 1];
            }
        }

        MPI_Gatherv(combined_msg.data(), local_concat_size, MPI_CHAR, all_fragments.data(), size_of_fragments.data(), indexes.data(), MPI_CHAR, 0, MPI_COMM_WORLD);

        std::vector<int> all_lengths;
        int local_num_strings = local_msg_size.size();
        std::vector<int> num_strings_per_process(total_processes);

        MPI_Gather(&local_num_strings, 1, MPI_INT, num_strings_per_process.data(), 1, MPI_INT, 0, MPI_COMM_WORLD);

        if (mpi_rank == 0) {
            int total_num_strings = std::accumulate(num_strings_per_process.begin(), num_strings_per_process.end(), 0);
            all_lengths.resize(total_num_strings);

            indexes[0] = 0;
            for (size_t i = 1; i < total_processes; ++i) {
                indexes[i] = indexes[i - 1] + num_strings_per_process[i - 1];
            }
        }

        MPI_Gatherv(local_msg_size.data(), local_num_strings, MPI_INT, all_lengths.data(), num_strings_per_process.data(), indexes.data(), MPI_INT, 0, MPI_COMM_WORLD);

        if (mpi_rank == 0) {
            std::vector<std::string> all_strings;
            int index = 0;
            for (int length : all_lengths) {
                all_strings.emplace_back(all_fragments.begin() + index, all_fragments.begin() + index + length);
                index += length;
            }

            std::sort(all_strings.begin(), all_strings.end());
            std::cout << tick << ":";
            for (const auto& str : all_strings) {
                std::cout << " " << str;
            }
            std::cout << std::endl;
        }
    }
}