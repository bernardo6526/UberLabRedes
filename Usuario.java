
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Usuario {

/*ideia : fazer um switch case que faz o cliente agir de forma diferente com base na mensagem recebida
  pelo pacote enviado pelo servidor.
*/
    // MÉTODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {

            int idClient= (int)(Math.random() * (10000 - 1 + 1) + 1);
            String tipoUser = "usuario";
            boolean ocupado = false;
			double latitude= (Math.random() * (20000 - 1 + 1) + 1);
			double longitude= (Math.random() * (20000 - 1 + 1) + 1);
            
            //System.out.println("sou o client: "+idClient);
			Scanner sc = new Scanner(System.in); // Create a Scanner object
			int comando;
			boolean exit = false;
			System.out.println("Iniciando o Uber "+tipoUser);

			while (!exit) {
				System.out.println("0-Sair\n1-Chamar Uber\n");
				comando = sc.nextInt();
				switch (comando) {
				case 0:
					exit = true;
					break;
				case 1:
					int newPort = conexaoInicial(idClient,tipoUser,ocupado,latitude,longitude);
					conexao(idClient,tipoUser,ocupado,newPort);
					break;
				default:
					System.out.println("comando não reconhecido");

				}
			}
			sc.close();
		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-C- O seguinte problema ocorreu : \n" + e.toString());
		}
	}

	public static int conexaoInicial(int idClient, String tipoUser,boolean ocupado,double latitude,double longitude) throws Exception {
        System.out.println("Procurando motorista...");
		
		// ENDEREÇO DO SERVIDOR
		String IPServidor = "25.19.211.24";
		int PortaServidor = 6000;
		int PortaCliente = 6001;

		// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
		DatagramSocket ds = new DatagramSocket(PortaCliente);
		System.out.println("-C- Cliente estabelecendo servico UDP (P" + PortaCliente + ")...");

		// CRIA UM PACOTE E ENVIA PARA O SERVIDOR
		String strEnvio = idClient+"/"+tipoUser+"/"+ocupado+"/"+latitude+"/"+longitude;
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(IPServidor),
				PortaServidor);
		System.out.println("-C- Enviando mensagem (IP:" + IPServidor + " - P:" + PortaServidor + ")...:" + strEnvio);
		ds.send(pktEnvio);

        //paramos aqui

		// CRIA UM PACOTE E RECEBE DADOS DO SERVIDOR
		byte[] bytRec = new byte[100];
		DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
		System.out.println("-C- Recebendo mensagem...");
		ds.receive(pktRec);
		bytRec = pktRec.getData();
		String strRet = new String(bytRec, 0, bytRec.length);

		// PROCESSA O PACOTE RECEBIDO
		//System.out.println("DEBUG |" + strRet+"|");
		strRet = strRet.trim(); // remove os espaços em branco da string recebida
		int newPort = Integer.parseInt(strRet);

		// FINALIZA O SERVIÇO UDP
		ds.close();
		System.out.println("-C- Conexao finalizada...");

		return newPort; // retorna o valor da porta
	}

	public static int conexao(int idClient, String tipoUser,boolean ocupado, int newPort) throws Exception {
        System.out.println("Procurando motorista...");
		
		// ENDEREÇO DO SERVIDOR
		String IPServidor = "25.19.211.24";
		int PortaServidor = newPort;
		int PortaCliente = 6002;

		// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
		DatagramSocket ds = new DatagramSocket(PortaCliente);
		System.out.println("-C- Cliente estabelecendo servico UDP (P" + PortaCliente + ")...");

		// CRIA UM PACOTE E ENVIA PARA O SERVIDOR
		String strEnvio = idClient+"/"+tipoUser+"/"+ocupado;
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(IPServidor),
				PortaServidor);
		System.out.println("-C- Enviando mensagem (IP:" + IPServidor + " - P:" + PortaServidor + ")...:" + strEnvio);
		ds.send(pktEnvio);

        //paramos aqui

		// CRIA UM PACOTE E RECEBE DADOS DO SERVIDOR
		byte[] bytRec = new byte[100];
		DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
		System.out.println("-C- Recebendo mensagem...");
		ds.receive(pktRec);
		bytRec = pktRec.getData();
		String strRet = new String(bytRec, 0, bytRec.length);

		// PROCESSA O PACOTE RECEBIDO
		System.out.println("-C- Mensagem recebida: " + strRet);

		// FINALIZA O SERVIÇO UDP
		ds.close();
		System.out.println("-C- Conexao finalizada...");

		return 0; // retorna o valor da porta
	}

	/*
	public String lerPacote(byte[] pacote){
		String retorno = "";
		for(int i=0;i<pacote.length;i++){
			retorno+=pacote[i];
		}
		return retorno;
	} */
}