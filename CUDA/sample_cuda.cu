#include "kseq/kseq.h"
#include "common.h"

#include <iostream>
#include <unordered_map>

const size_t MAX_SEQ_LENGTH = 200001; 

struct DNA {
  char seq[MAX_SEQ_LENGTH];
  int seq_len;
  char qual[MAX_SEQ_LENGTH];
};

struct MatchPair {
  int sample_id;
  int signature_id;
  double match_score;
};


__global__ void matcher(DNA* d_samples, DNA* d_signatures, MatchPair* d_matches, int* d_matches_count, int N, int M) {
  __shared__ int partial_matches_count;
  __shared__ int starting_index;
  if (threadIdx.x == 0) {
    partial_matches_count = 0;
    starting_index = 0;
  }
  __syncthreads();
  
  if (blockIdx.x < N && threadIdx.x < M) {
    bool match = false;
    int thread_index = 0;
    int limit = d_samples[blockIdx.x].seq_len - d_signatures[threadIdx.x].seq_len;
    double score = 0;
    for (int j = 0; j <= limit; j++) {
      for (int k = 0; k < d_signatures[threadIdx.x].seq_len; k++) {
        if (d_samples[blockIdx.x].seq[j + k] != 'N' && d_signatures[threadIdx.x].seq[k] != 'N' && d_samples[blockIdx.x].seq[j + k] != d_signatures[threadIdx.x].seq[k]) {
          // not a match, exit loop
          score = 0;
          break;
        }
        score += d_samples[blockIdx.x].qual[j + k] - 33;
        if (k == d_signatures[threadIdx.x].seq_len - 1) {
          thread_index = atomicAdd(&partial_matches_count, 1);
          match = true;
        }
      }
      if (match) {
        break;
      }
    }
    __syncthreads();
    
    if (threadIdx.x == 0) {
      starting_index = atomicAdd(d_matches_count, partial_matches_count);
    }
    __syncthreads();
    if (match) {
      double confidence_score = score / d_signatures[threadIdx.x].seq_len;
      MatchPair pair;
      pair.sample_id = blockIdx.x;
      pair.signature_id = threadIdx.x;
      pair.match_score = confidence_score;
      d_matches[starting_index + thread_index] = pair;
    }
  }
}

void runMatcher(const std::vector<klibpp::KSeq>& samples, const std::vector<klibpp::KSeq>& signatures, std::vector<MatchResult>& matches) {
  int N = samples.size();
  int M = signatures.size();

  std::vector<DNA> h_samples(N);
  std::vector<DNA> h_signatures(M);
  std::vector<MatchPair> h_matches(N * M);
  int h_matches_count = 0;

  for (size_t i = 0; i < N; i++) {
    DNA dna;
    std::string seq = samples[i].seq;
    int seq_len = seq.length();
    strncpy(dna.seq, seq.c_str(), seq_len);
    dna.seq[seq_len] = '\0'; 
    strncpy(dna.qual, samples[i].qual.c_str(), seq_len);
    dna.qual[seq_len] = '\0';
    dna.seq_len = seq_len;
    h_samples[i] = dna;
  }

  for (size_t i = 0; i < M; i++) {
    DNA dna;
    std::string seq = signatures[i].seq;
    int seq_len = seq.length();
    strncpy(dna.seq, seq.c_str(), seq_len);
    dna.seq[seq_len] = '\0'; 
    strncpy(dna.qual, signatures[i].qual.c_str(), seq_len);
    dna.qual[seq_len] = '\0'; 
    dna.seq_len = seq_len;
    h_signatures[i] = dna;
  }

  DNA* d_samples;
  DNA* d_signatures;
  MatchPair* d_matches;
  int* d_matches_count;

  cudaMalloc((void**)&d_samples, N * sizeof(DNA));
  cudaMalloc((void**)&d_signatures, M * sizeof(DNA));
  cudaMalloc((void**)&d_matches, N * M * sizeof(MatchPair));
  cudaMalloc((void**)&d_matches_count, sizeof(int));

  cudaMemcpyAsync(d_samples, h_samples.data(), N * sizeof(DNA), cudaMemcpyHostToDevice);
  cudaMemcpyAsync(d_signatures, h_signatures.data(),  M * sizeof(DNA), cudaMemcpyHostToDevice);
  cudaMemcpyAsync(d_matches, h_matches.data(),  N * M * sizeof(MatchPair), cudaMemcpyHostToDevice);
  cudaMemcpyAsync(d_matches_count, &h_matches_count,  sizeof(int), cudaMemcpyHostToDevice);
  
  matcher<<<N, M>>>(d_samples, d_signatures, d_matches, d_matches_count, N, M);

  cudaDeviceSynchronize();

  cudaMemcpyAsync(&h_matches_count, d_matches_count,  sizeof(int), cudaMemcpyDeviceToHost);
  cudaMemcpyAsync(h_matches.data(), d_matches,  h_matches_count * sizeof(MatchPair), cudaMemcpyDeviceToHost);

  for (int i = 0; i < h_matches_count; i++) {
    MatchPair pair = h_matches[i];
    MatchResult match_result;
    match_result.sample_name = samples[pair.sample_id].name;
    match_result.signature_name = signatures[pair.signature_id].name;
    match_result.match_score = pair.match_score;
    matches.push_back(match_result);
  }

  cudaFree(d_samples);
  cudaFree(d_signatures);
  cudaFree(d_matches);
  cudaFree(d_matches_count);
}
