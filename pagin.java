package pagingLab;
import java.util.*;
import java.io.*;
public class Pagin {
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
			processList[1] = new process(1, 1.0, 0.0, 0.0, 111%processSize);
		}
		else
			processList = new process[5];
		
		if(J==2){
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 1.0, 0.0, 0.0, (111*i)%processSize);
		}
		if(J==3){
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 0.0, 0.0, 0.0, (111*i)%processSize);
		}
		if(J==4){
			processList[1] = new process(1, 0.75, 0.25, 0.0, (111)%processSize);
			processList[2] = new process(2, 0.75, 0.0, 0.25, (111*2)%processSize);
			processList[3] = new process(3, 0.75, 0.125, 0.125, (111*3)%processSize);
			processList[4] = new process(4, 0.5, 0.125, 0.125, (111*4)%processSize);
		}
		
		frame frameTable[] = new frame[machineSize/pageSize];
		for(int i=0; i<frameTable.length; i++){
			frameTable[i] = new frame();
			frameTable[i].filled = false;
		}
		
		int currProcess = 1;
		processList[1].quant = q;
		
		for(int t=0; t<(processList.length-1)*numRefs; t++){	
			if(processList[currProcess].quant==0){
				if(currProcess==processList.length-1)
					currProcess = 1;
				else
					currProcess++;
				processList[currProcess].quant=q;
			}
			int tmp = processList[currProcess].ref;
			double y = rand.nextDouble()/Integer.MAX_VALUE+1d;
			
			if(y<processList[currProcess].a)
				processList[currProcess].ref = (tmp+1+processSize)%processSize;
			else if(y<processList[currProcess].a+processList[currProcess].b)
				processList[currProcess].ref = (tmp-5+processSize)%processSize;
			else if(y<processList[currProcess].a+processList[currProcess].b+processList[currProcess].c)
				processList[currProcess].ref = (tmp+4+processSize)%processSize;
			else
				processList[currProcess].ref = (rand.nextInt()*(processSize-1))%processSize;
			
			
			processList[currProcess].quant--;
		}
		
	}
}
class process{
	int pNumber;
	double a;
	double b;
	double c;
	int quant;
	int ref;
	
	process(int p, double A, double B, double C, int reference){
		pNumber = p;
		a=A;
		b=B;
		c=C;
		quant=0;
		ref = reference;
	}
}

class frame{
	int process;
	int pageNumber;
	int startTime;
	boolean filled;
}