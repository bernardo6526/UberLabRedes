public class tenis extends Thread {
	private String nome;
	private int time;	
	private static int ultimo_time=0;
	
	public tenis(String nome,int time) {
		this.nome= nome;
		this.time= time;		
	}

	public void run() {			
			for(int i= 0; i < 30; i++) {
				critical(nome,time);
				try
            {
                Thread.sleep(1000);
            } 
            catch (InterruptedException ex)
            {
                return;
            }
			}		
	}

	public static synchronized void critical(String nome, int time){
        if (time != ultimo_time){
						ultimo_time=time;
						System.out.print("\n" + nome + " time "+time+" REBATE!");
				}	
	}				
	
	public static void main(String [] args) {
		Thread t1= new tenis("Bernardo",0);
		Thread t2= new tenis("Tulio",0);
		Thread t3= new tenis("Vernardo",1);
		Thread t4= new tenis("Nulio",1);
		t1.start();
		t2.start();
		t3.start();
		t4.start();
	}
}
