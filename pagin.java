package pagingLab;
import java.util.*;
import java.io.*;
public class pagin {
	public static void main (String[] args){
		int machineSize = Integer.valueOf(args[0]);
		int pageSize = Integer.valueOf(args[1]);
		int processSize = Integer.valueOf(args[2]);
		int J = Integer.valueOf(args[3]);
		int numRefs = Integer.valueOf(args[4]);
		String r = args[5];
		int q = 3;
		
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
		
		process processList[];
		if(J==1){
			processList = new process[2];
			processList[1] = new process(1, 1.0, 0.0, 0.0, -1, numRefs);
		}
		else
			processList = new process[5];
		
		if(J==2){
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 1.0, 0.0, 0.0, -1, numRefs);
		}
		if(J==3){
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 0.0, 0.0, 0.0, -1, numRefs);
		}
		if(J==4){
			processList[1] = new process(1, 0.75, 0.25, 0.0, -1, numRefs);
			processList[2] = new process(2, 0.75, 0.0, 0.25, -1, numRefs);
			processList[3] = new process(3, 0.75, 0.125, 0.125, -1, numRefs);
			processList[4] = new process(4, 0.5, 0.125, 0.125, -1, numRefs);
		}
		
		frame frameTable[] = new frame[machineSize/pageSize];
		for(int i=0; i<frameTable.length; i++){
			frameTable[i] = new frame();
			frameTable[i].filled = false;
		}
		
		int currProcess = 1;
		processList[1].quant = q;
		//int totalResidency=0;
		int t=1;
		while(t<(processList.length-1)*numRefs+1){
			if(processList[currProcess].quant==0||processList[currProcess].refsLeft==0){
				int candidate =currProcess;
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
				currProcess = candidate;
				processList[currProcess].quant=q;
			}
			
			if(processList[currProcess].ref==-1){
				processList[currProcess].ref = 111*currProcess%processSize;
			}
			else{
				int tmp = processList[currProcess].ref;
				double y = rand.nextInt();
				System.out.println(y);
				y=y/2147483648.0;
				
				if(y<processList[currProcess].a)
					processList[currProcess].ref = (tmp+1+processSize)%processSize;
				else if(y<processList[currProcess].a+processList[currProcess].b){
					processList[currProcess].ref = (tmp-5+processSize*10)%processSize;
					//System.out.println("b");
				}
				else if(y<processList[currProcess].a+processList[currProcess].b+processList[currProcess].c){
					processList[currProcess].ref = (tmp+4+processSize)%processSize;
					//System.out.println("c");
				}
				else
					processList[currProcess].ref = rand.nextInt()%processSize;
					//System.out.println("d");
			}
			System.out.println(currProcess+" references word "+processList[currProcess].ref+" at time "+(t));
						
			int page = processList[currProcess].ref/pageSize;
			boolean inThere = false;
			for(int i=frameTable.length-1; i>=0; i--){
				if(!frameTable[i].filled && !inThere && !(frameTable[i].process==currProcess && frameTable[i].pageNumber==page)){
					frameTable[i].filled=true;
					frameTable[i].process = currProcess;
					frameTable[i].pageNumber=page;
					frameTable[i].startTime = t;
					frameTable[i].tLastUsed = t;
					System.out.println("process "+currProcess+ " page "+page+" placed in frame "+i);

					processList[frameTable[i].process].numFaults++;
					inThere = true;
				}
				else if(frameTable[i].process==currProcess && frameTable[i].pageNumber==page){
					inThere = true;
					frameTable[i].tLastUsed=t;
				}
			}
			if(!inThere){
				processList[currProcess].numFaults++;
				if(r.equals("lru")){
					int leastRecent=0;
					for(int frameNum=1; frameNum<frameTable.length; frameNum++){
						if(frameTable[frameNum].tLastUsed<frameTable[leastRecent].tLastUsed)
							leastRecent = frameNum;
					}//end for
					processList[frameTable[leastRecent].process].residency+=(t-frameTable[leastRecent].startTime);
					processList[frameTable[leastRecent].process].numEvictions++;
					//totalResidency+=t-frameTable[leastRecent].startTime;
					System.out.println("process "+frameTable[leastRecent].process+ " page "+frameTable[leastRecent].pageNumber+" evicted from frame "+leastRecent);
					
					frameTable[leastRecent].process = currProcess;
					frameTable[leastRecent].pageNumber=page;
					frameTable[leastRecent].startTime = t;
					frameTable[leastRecent].tLastUsed = t;
				}//end lru procedure
				if(r.equals("lifo")){
					int lastIn=0;
					for(int frameNum=1; frameNum<frameTable.length; frameNum++){
						if(frameTable[frameNum].startTime>frameTable[lastIn].startTime)
							lastIn = frameNum;
					}//end for
					processList[frameTable[lastIn].process].residency+=(t-frameTable[lastIn].startTime);
					processList[frameTable[lastIn].process].numEvictions++;
					//totalResidency+=t-frameTable[leastRecent].startTime;
					System.out.println("process "+frameTable[lastIn].process+ " page "+frameTable[lastIn].pageNumber+" evicted from frame "+lastIn);
					
					frameTable[lastIn].process = currProcess;
					frameTable[lastIn].pageNumber=page;
					frameTable[lastIn].startTime = t;
					frameTable[lastIn].tLastUsed = t;					
				}
				if(r.equals("random")){
					int random = rand.nextInt();
					System.out.println(random);
					random = random%frameTable.length;
					processList[frameTable[random].process].residency+=(t-frameTable[random].startTime);
					processList[frameTable[random].process].numEvictions++;
					//totalResidency+=t-frameTable[leastRecent].startTime;
					System.out.println("process "+frameTable[random].process+ " page "+frameTable[random].pageNumber+" evicted from frame "+random);
					
					frameTable[random].process = currProcess;
					frameTable[random].pageNumber=page;
					frameTable[random].startTime = t;
					frameTable[random].tLastUsed = t;	
				}
			}
			processList[currProcess].refsLeft--;
			processList[currProcess].quant--;
			t++;
		}
		/*for(int i=0; i<frameTable.length; i++){
			//processList[frameTable[i].process].residency+=t-frameTable[i].startTime;
			totalResidency+=t-frameTable[i].startTime-1;
		}*/
		rand.close();
		System.out.println("The machine size is "+machineSize);
		System.out.println("The page size is "+pageSize);
		System.out.println("The process size is "+processSize);
		System.out.println("The job mix number is "+J);
		System.out.println("The number of references per process is "+numRefs);
		System.out.println("The replacement algorithm is "+r);
		System.out.println();
		int totalFaults = 0;
		int totalResidency=0;
		int totalEvictions=0;
		for(int i=1; i<processList.length; i++){
			totalFaults+=processList[i].numFaults; 
			totalResidency+=processList[i].residency;
			totalEvictions += processList[i].numEvictions;
			double avgResidency = ((double)processList[i].residency)/((double)(processList[i].numEvictions));
			System.out.println("Process "+i+" had "+processList[i].numFaults+" faults and "+avgResidency+" average residency");
		}
		System.out.println();
		System.out.println("The total number of faults is "+totalFaults+" and the overall average residency is "+((double)totalResidency)/((double)totalEvictions));
	}
}
class process{
	int pNumber;
	double a;
	double b;
	double c;
	int quant;
	int ref;
	int numFaults;
	int numEvictions;
	int residency;
	int refsLeft;
	
	process(int p, double A, double B, double C, int reference, int rLeft){
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
	}
}

class frame{
	int process;
	int pageNumber;
	int startTime;
	int tLastUsed;
	boolean filled;
}