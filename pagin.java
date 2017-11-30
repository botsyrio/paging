
//David A. Foley
//Operating Systems, Prof. Gottlieb
//Lab 4, Paging
//11.28.17
import java.util.*;
import java.io.*;
public class Pagin {
	public static void main (String[] args){
		int machineSize = Integer.valueOf(args[0]);//read all input
		int pageSize = Integer.valueOf(args[1]);
		int processSize = Integer.valueOf(args[2]);
		int J = Integer.valueOf(args[3]);
		int numRefs = Integer.valueOf(args[4]);
		String r = args[5];
		int q = 3;//set the size of the quantum
		
		String randFilePath=System.getProperty("user.dir");//build path to random number file
		if(randFilePath.contains("\\"))//case where system separates directories with token "\\"
			randFilePath+="\\";
		else
			randFilePath+="/";//case where system separates directories with token "/"
		randFilePath+="random-numbers.txt";
		
		File randy=new File(randFilePath);
		Scanner rand;
		try {//initialize scanner which reads random number file
			rand = new Scanner (randy);
		} catch (FileNotFoundException e) {//if file is not found, terminate the program
			System.out.println("FATAL ERROR: file random-numbers.txt not found at "+randFilePath);
			e.printStackTrace();
			return;
		}
		
		process processList[];//this will hold all the processes in the system: at index i will be process i
		//initialize all processes in the system
		if(J==1){//case where job mix is 1
			processList = new process[2];
			processList[1] = new process(1, 1.0, 0.0, 0.0, 111%processSize, numRefs);
		}//end if 
		else//in all other cases, there will be 4 processes, so we can initialize the process list
			processList = new process[5];
		
		if(J==2){//initialize processes in case where job mix is 2
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 1.0, 0.0, 0.0, (111*i)%processSize, numRefs);
		}//end if
		if(J==3){//initialize processes in case where job mix is 3
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 0.0, 0.0, 0.0, (111*i)%processSize, numRefs);
		}//end if
		if(J==4){//initialize processes in case where job mix is 4
			processList[1] = new process(1, 0.75, 0.25, 0.0, (111*1)%processSize, numRefs);
			processList[2] = new process(2, 0.75, 0.0, 0.25, (111*2)%processSize, numRefs);
			processList[3] = new process(3, 0.75, 0.125, 0.125, (111*3)%processSize, numRefs);
			processList[4] = new process(4, 0.5, 0.125, 0.125, (111*4)%processSize, numRefs);
		}//end if
		
		frame frameTable[] = new frame[machineSize/pageSize];//initialize the frame table
		for(int i=0; i<frameTable.length; i++){
			frameTable[i] = new frame();
			frameTable[i].filled = false;//set the valid bit to 0 so the system knows we're starting with empty frames
		}//end for
		
		int currProcess = 1;//holds index of the process generating references at any given time
		processList[1].quant = q;//initialize the quantum in the process
		int t=1;//holds the time; initialized to 1
		while(t<(processList.length-1)*numRefs+1){//run the simulation
			if(processList[currProcess].quant==0||processList[currProcess].refsLeft==0){//case where a new process must run
				int candidate =currProcess;//holds what will be the next process to run
				if(candidate==processList.length-1)
					candidate = 1;
				else
					candidate++;
				while(processList[candidate].refsLeft==0){
					if(candidate==processList.length-1)
						candidate = 1;
					else
						candidate++;
				}
				currProcess = candidate;//update current process
				processList[currProcess].quant=q;//set the quantum
			}//end if
						
			int page = processList[currProcess].ref/pageSize;//the page number of the next word to be referenced by the current process 
			boolean inThere = false;//will be set to true if the desired page (is in/can be put in) the frame table; set to false otherwise
			for(int i=frameTable.length-1; i>=0; i--){//determine if the desired page is in the frame table/if there is an empty frame
				if(!frameTable[i].filled && !inThere && !(frameTable[i].process==currProcess && frameTable[i].pageNumber==page)){//case where an empty frame is found
					frameTable[i].filled=true;//indicate something is in the frame
					frameTable[i].process = currProcess;//set values of the frame
					frameTable[i].pageNumber=page;
					frameTable[i].startTime = t;
					frameTable[i].tLastUsed = t;

					processList[frameTable[i].process].numFaults++;//increment the number of faults the process has
					inThere = true;//indicate a frame has been found
				}//end if
				else if(frameTable[i].process==currProcess && frameTable[i].pageNumber==page){//case where the desired page is already in the frame table
					inThere = true;
					frameTable[i].tLastUsed=t;//set the time of the page's most recent use
				}//end if
			}//end for
			
			if(!inThere){//case where a frame must be replaced
				processList[currProcess].numFaults++;//increment the number of faults the process has undergone
				if(r.equals("lru")){
					int leastRecent=0;//will hold the index of the least recently used frame
					for(int frameNum=1; frameNum<frameTable.length; frameNum++){//find least recently used frame
						if(frameTable[frameNum].tLastUsed<frameTable[leastRecent].tLastUsed)
							leastRecent = frameNum;
					}//end for
					processList[frameTable[leastRecent].process].residency+=(t-frameTable[leastRecent].startTime);//update the evicted process's residency
					processList[frameTable[leastRecent].process].numEvictions++;//increment the number of times the evicted process has been evicted
					
					frameTable[leastRecent].process = currProcess;//update the frame with the information about the new page
					frameTable[leastRecent].pageNumber=page;
					frameTable[leastRecent].startTime = t;
					frameTable[leastRecent].tLastUsed = t;
				}//end lru procedure
				if(r.equals("lifo")){
					int lastIn=0;//will hold the index in the frame table of the last process inserted
					for(int frameNum=1; frameNum<frameTable.length; frameNum++){//find last frame in
						if(frameTable[frameNum].startTime>frameTable[lastIn].startTime)
							lastIn = frameNum;
					}//end for
					processList[frameTable[lastIn].process].residency+=(t-frameTable[lastIn].startTime);//update the evicted process's residency
					processList[frameTable[lastIn].process].numEvictions++;//increment the number of times the evicted process has been evicted
					
					frameTable[lastIn].process = currProcess;//update the frame with the information about the new page
					frameTable[lastIn].pageNumber=page;
					frameTable[lastIn].startTime = t;
					frameTable[lastIn].tLastUsed = t;					
				}//end lifo procedure
				if(r.equals("random")){
					int random = rand.nextInt();//hold the index of the randomly selected victim
					random = random%frameTable.length;
					
					processList[frameTable[random].process].residency+=(t-frameTable[random].startTime);//update the evicted process's residency
					processList[frameTable[random].process].numEvictions++;//increment the number of times the evicted process has been evicted
					
					frameTable[random].process = currProcess;//update the frame with the information about the new page
					frameTable[random].pageNumber=page;
					frameTable[random].startTime = t;
					frameTable[random].tLastUsed = t;	
				}//end random procedure
			}
			int tmp = processList[currProcess].ref;//temporary value holds last word referenced by the currenct process
			double y = rand.nextInt();//dice roll to figure out which procedure the process should use to get the next word
			y=y/2147483648.0;
			
			if(y<processList[currProcess].a)//update word to be referenced according to case a
				processList[currProcess].ref = (tmp+1+processSize)%processSize;
			else if(y<processList[currProcess].a+processList[currProcess].b){//update word to be referenced according to case b
				processList[currProcess].ref = (tmp-5+processSize*10)%processSize;
			}
			else if(y<processList[currProcess].a+processList[currProcess].b+processList[currProcess].c){//update word to be referenced according to case c
				processList[currProcess].ref = (tmp+4+processSize)%processSize;
			}
			else//update word to be referenced according to case 1-a-b-c
				processList[currProcess].ref = rand.nextInt()%processSize;
			processList[currProcess].refsLeft--;//update the number of references the current process still has to make
			processList[currProcess].quant--;//update the time left in the quantum
			t++;//increment time
		}//end while
		rand.close();
		//echo input
		System.out.println("The machine size is "+machineSize);
		System.out.println("The page size is "+pageSize);
		System.out.println("The process size is "+processSize);
		System.out.println("The job mix number is "+J);
		System.out.println("The number of references per process is "+numRefs);
		System.out.println("The replacement algorithm is "+r);
		System.out.println();
		int totalFaults = 0;//total faults across all processes
		int totalResidency=0;//total residency across all pages of all processes
		int totalEvictions=0;//total number of evictions across all pages of all processes
		//print output for each process and 
		for(int i=1; i<processList.length; i++){
			totalFaults+=processList[i].numFaults;//increment totals 
			totalResidency+=processList[i].residency;
			totalEvictions += processList[i].numEvictions;
			
			double avgResidency = ((double)processList[i].residency)/((double)(processList[i].numEvictions));//calculate process's average residency
			System.out.println("Process "+i+" had "+processList[i].numFaults+" faults and "+avgResidency+" average residency");//print output for process i
		}//end for
		System.out.println();
		System.out.println("The total number of faults is "+totalFaults+" and the overall average residency is "+((double)totalResidency)/((double)totalEvictions));//print overall output
	}//end main
}//end pagin class
class process{
	int pNumber;//process nuber
	double a;//probability procedure a will be used to get the next word to be referenced
	double b;//probability procedure b will be used to get the next word to be referenced
	double c;//probability procedure c will be used to get the next word to be referenced
	int quant;//time left before the process's quantum expires
	int ref;//next word to be referenced
	int numFaults;//number of times the process has faulted
	int numEvictions;//number of times the process has been evicted
	int residency;//process's total residency
	int refsLeft;//number of references the process still has to make
	
	process(int p, double A, double B, double C, int reference, int rLeft){//constructor
		pNumber = p;
		a=A;
		b=B;
		c=C;
		quant=0;
		ref = reference;
		residency = 0;
		numFaults = 0;
		numEvictions = 0;
		refsLeft = rLeft;
	}//end constructor
}//end process class

class frame{
	int process;//process number
	int pageNumber;//page number
	int startTime;//time the process was put in the frame table
	int tLastUsed;//time this frame was last accessed
	boolean filled;//valid bit
}//end frame class