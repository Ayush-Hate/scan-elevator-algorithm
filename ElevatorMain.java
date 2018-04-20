import java.util.*;

class SCAN {
	private static SCAN scan = null;
	private TreeSet<Integer> requestQueue = new TreeSet<Integer>();
	private int currentFloor = 0;//Initially elevator is at ground floor (0)
	private int dir = 1;//1 indicates UPWARDS MOTION 0 INDICATES DOWNWARD MOTION
	private Thread serviceThread;
	static SCAN getObj()
	{
		if(scan == null)
		{
			scan = new SCAN();
		}
		return scan;
	}
	
	public synchronized void newRequest(int floor)
	{
		requestQueue.add(floor);
		if(serviceThread.getState() == Thread.State.WAITING)
			notify();
		else
			serviceThread.interrupt();
	}
	
	public synchronized int moveElevator()
	{
		Integer floor=null;
		if( dir == 1)//if Direction is upwards
		{
			if(requestQueue.ceiling(currentFloor)!=null)
				floor = requestQueue.ceiling(currentFloor);
			else
				floor = requestQueue.floor(currentFloor);
		}
		else
		{
			if(requestQueue.floor(currentFloor)!=null)
				floor = requestQueue.floor(currentFloor);
			else
				floor = requestQueue.ceiling(currentFloor);
		}
		if(floor == null)
		{
			System.out.println("Elevator waiting (waiting for requests) at floor "+getFloor());
			try{
			wait();
			}catch(Exception e){}
		}
		else
		{
			requestQueue.remove(floor);
		}
		if(floor == null)
			return -1;
		else
			return floor;
	}
	
	public int getFloor()
	{
		return currentFloor;
	}
	
	public void setFloor(int requestFloor) throws Exception
	{
		if(currentFloor>requestFloor)
			dir=0;//set direction as DOWNWARDS
		else
			dir=1;//set direction as UPWARDS
		currentFloor=requestFloor;
		System.out.println("Elevator is travelling through floor "+currentFloor);
		Thread.sleep(2000);
	}
	
	public Thread getServiceThread()
	{
		return serviceThread;
	}
	
	public void setServiceThread(Thread st)
	{
		serviceThread=st;
	}

	public TreeSet<Integer> getQueue()
	{
		return requestQueue;
	}
}

class SCANResponse implements Runnable{
	public void run()
	{
		while(true) 
		{
			SCAN scan = SCAN.getObj();
			int floor = scan.moveElevator();
			int currentFloor=scan.getFloor();
			try{
				if(floor>=0)
				{
					if(currentFloor>floor)
					{
						while(currentFloor>floor)
						{
							currentFloor--;
							scan.setFloor(currentFloor);
						}
					}
					else
					{
						while(currentFloor<floor)
						{
							currentFloor++;
							scan.setFloor(currentFloor);
						}
					}
					System.out.println("Elevator is stationary at floor "+scan.getFloor());
					Thread.sleep(3000);
				}
			}catch(Exception e)
			{
				if(scan.getFloor()!=floor)
					scan.getQueue().add(floor);
			}
		}
	}
}

class Requests extends ElevatorMain implements Runnable{
	public void run()
	{
		while(true)
		{
			String floorRequest=null;
			Scanner ip=new Scanner(System.in);
			try{
				if(ip.hasNextLine())
					floorRequest = ip.nextLine();
			}catch(Exception e){}
			if(floorRequest!=null && Integer.parseInt(floorRequest)>=0 && Integer.parseInt(floorRequest)<=floors)
			{
				System.out.println("Elevator has been called at floor "+floorRequest);
				SCAN scan=SCAN.getObj();
				scan.newRequest(Integer.parseInt(floorRequest));
			}
			else
				System.out.println("Invalid input. Please enter an integer between 0 and "+floors);
		}
	}
}

public class ElevatorMain {
	
	public static int floors;
	
	public static void main(String[] args) {
		System.out.println("DEMONSTRATION OF ELEVATOR (SCAN) ALGORITHM\n\nEnter the number of floors: ");
		Scanner s=new Scanner(System.in);
		floors=s.nextInt();//Get the number of floors in the building (0-floors)
		System.out.println("You, the user, are at a floor within a "+floors+" story building. The elevator is stationary at the ground floor initially. In this simulation, you can, at any given time, enter a floor number to simulate a person calling the elevator at that floor. The elevator will respond to simultaneous requests based on the SCAN algorithm. \n\n");
		SCANResponse action=new SCANResponse();//Object of the class which contains the run thread for the SCAN algorithm processing 
		Requests calls=new Requests();//Object of the class which reads user input of floor levels
		Thread a=new Thread(action,"passenger");//Thread for the SCAN processor
		Thread b=new Thread(calls,"elevator");//Thread for user input
		SCAN.getObj().setServiceThread(a);//Sets the service thread for the SCAN class object
		b.start();//Start the user input thread
		a.start();//Start the SCAN algorithm thread
	}
}
