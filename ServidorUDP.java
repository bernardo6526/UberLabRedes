
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ServidorUDP {
	// METODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {

			for (int i = 0; i < 3; i++) {
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

	// salva as informações enviadas pelo pacote inicial de cada cliente
	private static ArrayList<DatagramPacket> clientes = new ArrayList<DatagramPacket>();
	// cria um array para armazenas as corridas
	private static ArrayList<ArrayList<DatagramPacket>> corridas = new ArrayList<ArrayList<DatagramPacket>>();

	public ClienteHandler() {
		// run(); -> inicializado automaticamente
	}

	public void run() {
		try {
			// gera um número aleatório para ser uma porta disponível
			int newPort = (int) (Math.random() * (10000 - 1 + 1) + 1);

			// abre a conexao recepcionista, para trocar mensagem com o cliente sobre qual
			// porta ele deverá usar
			String tipoUser = conexaoInicial(6000, newPort);

			if (tipoUser.equals("usuario")) { // diferencia a conexao pelo tipo de user
				System.out.println("DEBUG Conectando usuario");
				conexaoUsuario(newPort);
			} else {
				System.out.println("DEBUG Conectando motorista");
				conexaoMotorista(newPort);
			}

		} catch (Exception e) {  // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
			System.out.println("-S- O seguinte problema ocorreu : \n" + e.toString());
		}
	}

	// Impede que as threads acessem simultaneamente a conexão inicial, prevenindo
	// assim congestionamento na porta
	public static synchronized String conexaoInicial(int ServerPort, int newPort) {
		String tipoUser = ""; // inicializa a variavel tipoUsuario que será obtida com a info do pacote

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

			clientes.add(pktRec); // salva o pacote no array de clientes
			// System.out.println("DEBUG pacote adicionado");
			String[] pacote = pktProcessor(pktRec); // interpreta o pacote como string
			tipoUser = pacote[1]; // 0idClient,1tipoUser,2ocupado,3latitude,4longitude

			// PROCESSA O PACOTE RECEBIDO
			bytRec = pktRec.getData();
			String strMsg = new String(bytRec, 0, bytRec.length);
			System.out.println("-S- Mensagem recebida: " + strMsg);

			// Pega dados do Cliente
			InetAddress ipRet = pktRec.getAddress();
			int portaRet = pktRec.getPort();

			// CRIA UM PACOTE de RETORNO -> adicionar informações da porta nova que o client
			// pode se conectar
			String strMsgRet = newPort + "";
			byte[] bytRet = strMsgRet.getBytes();
			DatagramPacket pktEnv = new DatagramPacket(bytRet, bytRet.length, ipRet, portaRet);

			System.out.println("DEBUG " + strMsgRet);

			// E ENVIA PARA O CLIENTE
			System.out.println(
					"-S- Enviando mensagem (IP:" + ipRet.getHostAddress() + " - P:" + portaRet + " )...:" + strMsgRet);
			ds.send(pktEnv);

			// FINALIZA O SERVICO UDP
			ds.close();
			System.out.println("-S- Conexao finalizada...");

		} catch (Exception e) { // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
			System.out.println("-S- O seguinte problema na conexao inicial ocorreu : \n" + e.toString());
		}

		return tipoUser;
	}

	public static void conexaoUsuario(int serverPort) {
		try {

			// ESTABELECE UM SERVIÇO UDP NA NOVA PORTA ESPECIFICADA
			DatagramSocket ds = new DatagramSocket(serverPort);
			// System.out.println("-S- Servidor estabelecendo servico UDP (P:" + serverPort
			// + ")...");

			// CRIA UM PACOTE
			byte[] bytRec = new byte[100];
			// RECEBE DADOS DO CLIENTE
			DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
			// System.out.println("-S- Recebendo mensagem...");
			ds.receive(pktRec);
			
			// requisita de forma sincronizada no servidor um motorista e muda seu status
			// para ocupado
			// passa como parâmetro o pacote com as informações do usuário que está
			// procurando um motorista
			boolean motoristaDisponivel = false;
			DatagramPacket pacoteMotorista = null;
			while (!motoristaDisponivel) {
				pacoteMotorista = achaMotorista(pktRec);
				if (pacoteMotorista != null)motoristaDisponivel = true;
				Thread.sleep(400);
			}
			System.out.println("DEBUG liberdade");

			// Pega dados do Cliente
			InetAddress ipRet = pktRec.getAddress();
			int portaRet = pktRec.getPort();

			//Cria um novo pacote com as informações de um outro cliente, para possibilitar a conexão P2P
			DatagramPacket pktEnv = pktCreator(pacoteMotorista,ipRet,portaRet);
			ds.send(pktEnv);

			// FINALIZA O SERVICO UDP
			ds.close();
			System.out.println("-S- Conexao finalizada...");

		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-S- O seguinte problema na conexao usuario ocorreu : \n" + e.toString());
		}
	}

	public static void conexaoMotorista(int serverPort) {
		try {

			// ESTABELECE UM SERVIÇO UDP NA NOVA PORTA ESPECIFICADA
			DatagramSocket ds = new DatagramSocket(serverPort);
			// System.out.println("-S- Servidor estabelecendo servico UDP (P:" + serverPort
			// + ")...");

			// CRIA UM PACOTE
			byte[] bytRec = new byte[100];
			// RECEBE DADOS DO CLIENTE
			DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
			// System.out.println("-S- Recebendo mensagem...");
			ds.receive(pktRec);

			// requisita de forma sincronizada no servidor uma busca no array de corridas
			// passa como parâmetro o pacote com as informações do motorista que está
			// aguardando o usuário
			boolean achouCorrida = false;
			DatagramPacket usuario = null;
			// Verifica no vetor de corridas se um Usuário já solicitou o motorista atual
			while (!achouCorrida) {
				ArrayList<ArrayList<DatagramPacket>> cpCorridas = new ArrayList<ArrayList<DatagramPacket>>();
				cpCorridas = (ArrayList) corridas.clone();
				usuario = achaUsuario(pktRec, cpCorridas);
				if (usuario != null)
					achouCorrida = true;
				Thread.sleep(400);
			}

			// Pega dados do Cliente
			InetAddress ipRet = pktRec.getAddress();
			int portaRet = pktRec.getPort();

			//Cria um novo pacote com as informações de um outro cliente, para possibilitar a conexão P2P
			System.out.println("$$$$enviando pacote para o motorista");
			DatagramPacket pktEnv = pktCreator(usuario,ipRet,portaRet);
			ds.send(pktEnv);

			// FINALIZA O SERVICO UDP
			ds.close();
			System.out.println("-S- Conexao finalizada...");

		} catch (Exception e) // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println("-S- O seguinte problema na conexao motorista ocorreu : \n" + e.toString());
		}
	}

	//Cria um novo pacote com as informações de um outro cliente, para possibilitar a conexão P2P
	public static DatagramPacket pktCreator(DatagramPacket outroCliente, InetAddress ipDestino, int portaDestino){
		//Pega dados do novo cliente
		String[] strCliente = pktProcessor(outroCliente);
		InetAddress ipCliente = outroCliente.getAddress();
		int portaCliente = outroCliente.getPort();

		//0idClient,1tipoUser,2ocupado,3latitude,4longitude,5nome,6latDestino,7longDestino
		//CRIA UM PACOTE de RETORNO
		String strMsgRet = "";
		for(int i=0;i<strCliente.length;i++){
			strMsgRet+= strCliente[i]+"/";
		}
		strMsgRet+=ipCliente.getHostAddress()+"/"+portaCliente;
		System.out.println("$$$$$ipcliente "+ipCliente.getHostAddress());
		byte[] bytRet = strMsgRet.getBytes();
		DatagramPacket pktEnv = new DatagramPacket(bytRet, bytRet.length, ipDestino, portaDestino);

		// E ENVIA PARA O CLIENTE
		System.out.println(
				"-S- Enviando mensagem (IP:" + ipCliente.getHostAddress() + " - P:" + portaDestino + " )...:" + strMsgRet);
		return pktEnv;
	}

	public static String[] pktProcessor(DatagramPacket pacote) {
		byte[] dadosPacote = new byte[100];
		dadosPacote = pacote.getData(); // preenche o vetor de bytes com os dados do pacote
		String mensagem = new String(dadosPacote, 0, dadosPacote.length); // converte os dados para string
		mensagem = mensagem.trim(); // remove espaços em branco no final da string
		String[] info = mensagem.split("/"); // separa a string em idClient,tipoUser,ocupado,latitude,longitude;
		return info;
	}

	// 0idClient,1tipoUser,2ocupado,3latitude,4longitude
	// Calcula a distancia entre usuario e motorista
	public static double calcDist(String[] usuario, String[] motorista) {
		double x1 = Double.parseDouble(usuario[3]);
		double y1 = Double.parseDouble(usuario[4]);
		double x2 = Double.parseDouble(motorista[3]);
		double y2 = Double.parseDouble(motorista[4]);
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	public static DatagramPacket achaMotorista(DatagramPacket pktRec) {
		// Creating another linked list and copying
		ArrayList<DatagramPacket> cpClientes = new ArrayList<DatagramPacket>();
		cpClientes = (ArrayList) clientes.clone();

		// processa o pacote recebido
		String[] usuario = pktProcessor(pktRec);
		// cria uma variavel para marcar a posição do motorista mais próximo no array de
		// pacotes
		int motoristaMaisProximo = -1;

		// System.out.println("Preso no achaMotorista()");
		double menorDist = 1000000;
		for (int i = 0; i < cpClientes.size(); i++) {
			DatagramPacket pacote = cpClientes.get(i);
			String[] info = pktProcessor(pacote); // processa o pacote num array de string
			// 0idClient,1tipoUser,2ocupado,3latitude,4longitude,5nome
			System.out.println("DEBUG 260");
			System.out.println("DEBUG " + info[1] + "/" + info[2]);
			// se achar motorista que não esteja ocupado calcula a distancia
			if (info[1].equals("motorista") && !Boolean.parseBoolean(info[2])) {
				double dist = calcDist(usuario, info);
				// armazena o motorista mais proximo
				if (dist < menorDist) {
					menorDist = dist;
					motoristaMaisProximo = i;
				}
			}
		}

		DatagramPacket pacoteMotorista = null;

		if (motoristaMaisProximo != -1) {
			// mudar o status do usuario e do motorista para ocupado
			pacoteMotorista = cpClientes.get(motoristaMaisProximo);
			// clientes.set(motoristaMaisProximo, pacoteMotorista);

			// cria o par de conexao motorista-usuario
			ArrayList<DatagramPacket> parConexao = new ArrayList<DatagramPacket>();
			parConexao.add(pacoteMotorista);
			parConexao.add(pktRec);

			// adicionar no vetor de corridas o par de conexao criado
			corridas.add(parConexao);
		}
		return pacoteMotorista;
	}

	public static DatagramPacket achaUsuario(DatagramPacket pktRec, ArrayList<ArrayList<DatagramPacket>> cpCorridas) {
		// processa o pacote recebido
		String[] motorista = pktProcessor(pktRec);
		DatagramPacket usuario = null;
		for (int i = 0; i < cpCorridas.size(); i++) {
			// processa o pacote do motorista num array de string
			String[] info = pktProcessor(cpCorridas.get(i).get(0));

			/*
			 * OBS .: 0idClient,1tipoUser,2ocupado,3latitude,4longitude,5nome o par de conexao é
			 * no formato 0 motorista, 1 usuario
			 */

			// se o motorista encontrar seu id no array de corridas, retorna o pacote do
			// usuario
			if (info[0].equals(motorista[0])) {
				usuario = cpCorridas.get(i).get(1); // salva o pacote do usuario conectado ao motorista
			}
		}

		return usuario;
	}
}
