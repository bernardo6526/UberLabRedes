
import java.net.*;
import java.io.*;
import java.util.ArrayList;

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
	private static int serverPort;

	//salva as informações enviadas pelo pacote inicial de cada cliente
	private static ArrayList<DatagramPacket> clientes = new ArrayList<DatagramPacket>();

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

			clientes.add(pktRec); //salva o pacote no array de clientes

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
			//System.out.println("-S- Servidor estabelecendo servico UDP (P:" + serverPort + ")...");

			// CRIA UM PACOTE
			byte[] bytRec = new byte[100];
			// RECEBE DADOS DO CLIENTE
			DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
			//System.out.println("-S- Recebendo mensagem...");
			ds.receive(pktRec);

			// PROCESSA O PACOTE RECEBIDO
			bytRec = pktRec.getData();
			String strMsg = new String(bytRec, 0, bytRec.length);
			//System.out.println("-S- Mensagem recebida: " + strMsg);

			// Pega dados do Cliente
			InetAddress ipRet = pktRec.getAddress();
			int portaRet = pktRec.getPort();
			//---------------------------------------------------------
			//processa o pacote recebido
			String[] usuario = pktProcessor(pktRec);
			//cria uma variavel para marcar a posição do motorista mais próximo no array de apcotes
			int motoristaMaisProximo=0;
			// Procura por motorista mais próximo
			boolean motoristaDisponivel = false;
			while(!motoristaDisponivel){
				double menorDist = 1000000;
				int contador = 0;
				for (DatagramPacket pacote : clientes) { 		      
					String[] info = pktProcessor(pacote); // processa o pacote num array de string
					//0idClient,1tipoUser,2ocupado,3latitude,4longitude

					//se achar motorista que não esteja ocupado calcula a distancia
					if(info[2].equals("motorista") && !Boolean.parseBoolean(info[3])){
						motoristaDisponivel = true;
						double dist = calcDist(usuario,info);						
						//armazena o motorista mais proximo
						if(dist < menorDist){
							menorDist = dist;
							motoristaMaisProximo = contador;
						}
					}
					contador++;
				}
			}	

			//inicia corrida marcando o motorista e o usuario como ocupados
			//OBS.: essa parte deve estar num método synchronized, ou seja, numa região crítica
			//pois o array de pacotes está sendo alterado simultaneamente, gerando o erro
			//java.util.concurrentmodificationexception
			/*depois disso é preciso fazer um codigo do motorista que fica ouvindo seu proprio status
			  do servidor, e quando ele ver que ficou como ocupado irá receber mensagens do servidor com
			  a localização do usuario
			*/
			DatagramPacket pacoteMotorista = clientes.get(motoristaMaisProximo);
			clientes.set(motoristaMaisProximo, pacoteMotorista);



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

	public static String[] pktProcessor(DatagramPacket pacote){
		byte[] dadosPacote = new byte[100];
		dadosPacote = pacote.getData(); // preenche o vetor de bytes com os dados do pacote
		String mensagem = new String(dadosPacote, 0, dadosPacote.length); //converte os dados para string
		mensagem = mensagem.trim(); // remove espaços em branco no final da string
		String[] info = mensagem.split("/"); //separa a string em idClient,tipoUser,ocupado,latitude,longitude;
		return info;
	}

	//0idClient,1tipoUser,2ocupado,3latitude,4longitude
	//Calcula a distancia entre usuario e motorista
	public static double calcDist(String[] usuario, String[] motorista) {
		double x1 = Integer.parseInt(usuario[3]);
		double y1 = Integer.parseInt(usuario[4]);
		double x2 = Integer.parseInt(motorista[3]);
		double y2 = Integer.parseInt(motorista[4]);       
    	return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
}
