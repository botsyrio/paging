package pagingLab;

public class pagin {
	public static void main (String[] args){
		int machineSize = Integer.valueOf(args[0]);
		int pageSize = Integer.valueOf(args[1]);
		int processSize = Integer.valueOf(args[2]);
		int j = Integer.valueOf(args[3]);
		int n = Integer.valueOf(args[4]);
		String r = args[5];
		
		process processList[];
		if(j==1){
			processList = new process[2];
			processList[1] = new process(1, 1.0, 0.0, 0.0);
		}
		else
			processList = new process[5];
		if(j==2){
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 1.0, 0.0, 0.0);
		}
		if(j==3){
			for(int i =1; i<5; i++)
				processList[i] = new process(i, 0.0, 0.0, 0.0);
		}
		if(j==4){
			processList[1] = new process(1, 0.75, 0.25, 0.0);
			processList[2] = new process(2, 0.75, 0.0, 0.25);
			processList[3] = new process(3, 0.75, 0.125, 0.125);
			processList[4] = new process(4, 0.5, 0.125, 0.125);
		}
	}
}
class process{
	int pNumber;
	double a;
	double b;
	double c;
	int q;
	
	process(int p, double A, double B, double C){
		pNumber = p;
		a=A;
		b=B;
		c=C;
	}
}