
import java.net.*;
import java.io.*;

public class ServidorUDP {
	// METODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {
			// Porta na qual o servidor ira "escutar"
			int ServerPort = 6000;

			// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
			DatagramSocket serverds = new DatagramSocket(ServerPort);
			System.out.println("-S- Servidor estabelecendo servico UDP (P:" + ServerPort + ")...");

			// Abre a conexao no clienteHandler
			Thread t = new ClienteHandler(serverds);
			t.start();

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
	final DatagramSocket ds;
	private static int serverPort = 6000;

	public ClienteHandler(DatagramSocket ds) {
		this.ds = ds;
		this.serverPort++;
	}

	public void run() {
		try {
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

			// CRIA UM PACOTE de RETORNO
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
}