
import java.net.*;
import java.io.*;

public class ServidorUDP {
	// METODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {
				
			for(int i=0;i<3;i++){
				// Cria uma nova thread para atender uma conexão
				Thread t = new ClienteHandler();
				t.start();
			}	

		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-S- O seguinte problema ocorreu : \n" + e.toString());
		}
	}

}

class ClienteHandler extends Thread {

	private String tipoUser;
	private int idClient;
	private boolean ocupado; // define se o client ja foi associado a outro client
	//final DatagramSocket ds;
	private static int serverPort;

	public ClienteHandler() {
		
	}

	//Impede que as threads acessem simultaneamente a conexão inicial, prevenindo assim congestionamento na porta
	public static synchronized int conexaoInicial(int ServerPort, int newPort){
		try {
			// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
			DatagramSocket ds = new DatagramSocket(ServerPort);
			System.out.println("-S- Servidor estabelecendo servico UDP (P:" + ServerPort + ")...");

			// CRIA UM PACOTE
			byte[] bytRec = new byte[100];
			// RECEBE DADOS DO CLIENTE
			DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
			System.out.println("-S- Recebendo mensagem...");
			ds.receive(pktRec);

			// PROCESSA O PACOTE RECEBIDO
			bytRec = pktRec.getData();
			String strMsg = new String(bytRec, 0, bytRec.length);
			System.out.println("-S- Mensagem recebida: " + strMsg);

			// Pega dados do Cliente
			InetAddress ipRet = pktRec.getAddress();
			int portaRet = pktRec.getPort();

			// CRIA UM PACOTE de RETORNO -> adicionar informações da porta nova que o client pode se conectar
			String strMsgRet = newPort+"";
			byte[] bytRet = strMsgRet.getBytes();
			DatagramPacket pktEnv = new DatagramPacket(bytRet, bytRet.length, ipRet, portaRet);

			System.out.println("DEBUG "+strMsgRet);

			// E ENVIA PARA O CLIENTE
			System.out.println(
					"-S- Enviando mensagem (IP:" + ipRet.getHostAddress() + " - P:" + portaRet + " )...:" + strMsgRet);
			ds.send(pktEnv);

			// FINALIZA O SERVICO UDP
			ds.close();
			System.out.println("-S- Conexao finalizada...");

		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-S- O seguinte problema ocorreu : \n" + e.toString());
		}
		return newPort;
	}

	public static void conexao(int serverPort){
		try {
			
			//ESTABELECE UM SERVIÇO UDP NA NOVA PORTA ESPECIFICADA
			DatagramSocket ds = new DatagramSocket(serverPort);
			System.out.println("-S- Servidor estabelecendo servico UDP (P:" + serverPort + ")...");

			// CRIA UM PACOTE
			byte[] bytRec = new byte[100];
			// RECEBE DADOS DO CLIENTE
			DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
			System.out.println("-S- Recebendo mensagem...");
			ds.receive(pktRec);

			// PROCESSA O PACOTE RECEBIDO
			bytRec = pktRec.getData();
			String strMsg = new String(bytRec, 0, bytRec.length);
			System.out.println("-S- Mensagem recebida: " + strMsg);

			// Pega dados do Cliente
			InetAddress ipRet = pktRec.getAddress();
			int portaRet = pktRec.getPort();

			// CRIA UM PACOTE de RETORNO -> adicionar informações da porta nova que o client pode se conectar
			String strMsgRet = "RETORNO - " + strMsg;
			byte[] bytRet = strMsgRet.getBytes();
			DatagramPacket pktEnv = new DatagramPacket(bytRet, bytRet.length, ipRet, portaRet);

			// E ENVIA PARA O CLIENTE
			System.out.println(
					"-S- Enviando mensagem (IP:" + ipRet.getHostAddress() + " - P:" + portaRet + " )...:" + strMsgRet);
			ds.send(pktEnv);

			// FINALIZA O SERVICO UDP
			ds.close();
			System.out.println("-S- Conexao finalizada...");

		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-S- O seguinte problema ocorreu : \n" + e.toString());
		}
	}	

	public void run() {
		try {
			//gera um número aleatório para ser uma porta disponível
			int newPort = (int)(Math.random() * (10000 - 1 + 1) + 1);

			//abre a conexao recepcionista, para trocar mensagem com o cliente sobre qual porta ele deverá usar
			int handlerPort = conexaoInicial(6000,newPort);
			conexao(handlerPort);
			

		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-S- O seguinte problema ocorreu : \n" + e.toString());
		}
	}
}