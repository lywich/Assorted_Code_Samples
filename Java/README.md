This sub directory is short snippet of Java code that was used for a project

Traditionally, when a student submits code, their answer is verified by comparing the output of their code and the model answer. To be 100% certain of correctness, this may take over a 100 different inputs and outputs, and taking care of each edge cases while keeping in mind the student may hardcode answers. 

In addition, we also must consider time complexity of the program. Normally, a static time threshold is set to enforce this. However, this may not be feasible during peak traffic periods. 

This project seeks to change the way we validate code using Z3. By comparing the logic of the code with the model answer, we will then output if the answer is right. In this subdirectoy, you will see how we extract the logic out of the code and convert it to logic expressions. 

To give an idea of what is logically equivalent: 
1. The order of code statements do not matter unless there are dependencies present between the two statements or between them.
2. Parameter order do not matter in cases like "func(String str, int val)" and "func(int val, String str)".

While I did not include code samples we used for testing, the test direction should give a slight insight to cases we look out for. 