# CS1550-Project3
VirtualMemorySimulator

To Compile
$javac vmsim.java

To Run
$java vmsim –n <numframes> -a <opt|clock|aging|work> [-r <refresh>] <tracefile>

numframes - expects an integer for the number of frames the simulation is to be run with
opt - optimal algorithm
clock - clock algorithm
aging - aging algorithm
work - working set clock algorithm

-r - this is only used in algorithms that require a refresh interval such as aging

Info about Tracefiles:
All tracefiles should be in basic text form. This program is desined to read tracefiles where each line is in this format
<address> <R/W>
Example
0041f7a0 R
0041f7f0 w
0031e0a0 R
