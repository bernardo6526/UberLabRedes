
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Usuario {
	// atributos do usuario
	static int idClient = (int) (Math.random() * (10000 - 1 + 1) + 1);
	static String tipoUser = "usuario";
	static boolean ocupado = false;
	static double latitude = (Math.random() * (20000 - 1 + 1) + 1);
	static double longitude = (Math.random() * (20000 - 1 + 1) + 1);
	static String nome = "Natasha Romanoff";
	static double latDestino = (Math.random() * (20000 - 1 + 1) + 1);
	static double longDestino = (Math.random() * (20000 - 1 + 1) + 1);

	// infos para conectar ao servidor
	static String IPServidor = "25.19.211.24"; // ENDEREÇO DO SERVIDOR
	static int PortaServidor = 6000; // define qual porta do servidor será acessada
	static int PortaCliente = 6001; // define qual porta será usada para se comunicar com o Servidor

	// portas de conexao P2P
	static int PortaUsuario = 6003; // define qual porta será usada para se comunicar com o Motorista
	static int PortaMotorista = 6004; // define qual porta do cliente do Motorista será acessada

	/*
	 * ideia : fazer um switch case que faz o cliente agir de forma diferente com
	 * base na mensagem recebida pelo pacote enviado pelo servidor.
	 */
	// MÉTODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {
			Scanner sc = new Scanner(System.in); // Create a Scanner object
			int comando;
			boolean exit = false;
			System.out.println("Iniciando o Uber " + tipoUser);

			while (!exit) {
				System.out.println("0-Sair\n1-Chamar Uber\n");
				comando = sc.nextInt();
				switch (comando) {
				case 0:
					exit = true;
					break;
				case 1:
					int newPort = conexaoInicial(idClient, tipoUser, ocupado, latitude, longitude, nome, latDestino,
							longDestino);
					conexao(idClient, tipoUser, ocupado, latitude, longitude, nome, latDestino, longDestino, newPort);
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

	public static int conexaoInicial(int idClient, String tipoUser, boolean ocupado, double latitude, double longitude,
			String nome, double latDestino, double longDestino) throws Exception {
		System.out.println("Conectando ao servidor Uber...");

		// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
		DatagramSocket ds = new DatagramSocket(PortaCliente);
		// System.out.println("-C- Cliente estabelecendo servico UDP (P" + PortaCliente
		// + ")...");

		// CRIA UM PACOTE E ENVIA PARA O SERVIDOR
		String strEnvio = idClient + "/" + tipoUser + "/" + ocupado + "/" + latitude + "/" + longitude + "/" + nome
				+ "/" + latDestino + "/" + longDestino;
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(IPServidor),
				PortaServidor);
		// System.out.println("-C- Enviando mensagem (IP:" + IPServidor + " - P:" +
		// PortaServidor + ")...:" + strEnvio);
		ds.send(pktEnvio);

		// CRIA UM PACOTE E RECEBE DADOS DO SERVIDOR
		byte[] bytRec = new byte[100];
		DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
		// System.out.println("-C- Recebendo mensagem...");
		ds.receive(pktRec);
		bytRec = pktRec.getData();
		String strRet = new String(bytRec, 0, bytRec.length);

		// PROCESSA O PACOTE RECEBIDO
		strRet = strRet.trim(); // remove os espaços em branco da string recebida
		int newPort = Integer.parseInt(strRet);

		// FINALIZA O SERVIÇO UDP
		ds.close();

		return newPort; // retorna o valor da porta
	}

	public static void conexao(int idClient, String tipoUser, boolean ocupado, double latitude, double longitude,
			String nome, double latDestino, double longDestino, int newPort) throws Exception {
		System.out.println("Procurando motorista...");

		// Cria a variável para receber a nova porta de acesso do servidor
		int PortaServidor = newPort;

		// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
		DatagramSocket ds = new DatagramSocket(PortaCliente);
	

		// CRIA UM PACOTE E ENVIA PARA O SERVIDOR
		String strEnvio = idClient + "/" + tipoUser + "/" + ocupado + "/" + latitude + "/" + longitude + "/" + nome
				+ "/" + latDestino + "/" + longDestino;
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(IPServidor),
				PortaServidor);
		ds.send(pktEnvio);

		// CRIA UM PACOTE E RECEBE DADOS DO SERVIDOR
		byte[] bytRec = new byte[1000]; // valor aumentado de 100 para 1000 pois o buffer estava estourando
		DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
		// System.out.println("-C- Recebendo mensagem...");
		ds.receive(pktRec);
		bytRec = pktRec.getData();
		String strRet = new String(bytRec, 0, bytRec.length);

		// PROCESSA O PACOTE RECEBIDO
		strRet = strRet.trim();
		String[] motorista = strRet.split("/");
		
		// Envia as informações do motorista para realizar a conexão P2P
		corrida(motorista);

		// FINALIZA O SERVIÇO UDP
		ds.close();

	}

	public static void corrida(String[] motorista) {
		try {
			// Trata as informações do motorista para efetuar a troca de mensagens
			String ipMotorista = motorista[6];

			// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
			DatagramSocket ds = new DatagramSocket(PortaUsuario);

			// Recebe os pacotes durante a corrida, até a viagem ser encerrada
			while (!receberPacote(ds).equals("Viagem encerrada"));			;
			Thread.sleep(400);

			// Envia um pacote para o motorista
			enviarPacote("O pagamento foi efetuado", ipMotorista, ds);
			ds.close();
		} catch (Exception e) { // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
			System.out.println("-C- O seguinte problema ocorreu na conexao da corrida : \n" + e.toString());
		}

	}

	public static void enviarPacote(String strEnvio, String ipDestino, DatagramSocket ds) throws Exception {
		// CRIA UM PACOTE E ENVIA PARA O USUARIO
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(ipDestino),
				PortaMotorista);
		// System.out.println("-C- Enviando mensagem (IP:" + ipDestino + " - P:" +
		// PortaMotorista + ")...:" + strEnvio);
		ds.send(pktEnvio);
	}

	public static String receberPacote(DatagramSocket ds) throws Exception {
		// CRIA UM PACOTE E RECEBE DADOS DO SERVIDOR
		byte[] bytRec = new byte[100];
		DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
		ds.receive(pktRec);

		// PROCESSA O PACOTE
		bytRec = pktRec.getData();
		String strRet = new String(bytRec, 0, bytRec.length);
		strRet = strRet.trim();

		// IMPRIME A MENSAGEM 
		System.out.println(strRet);
		return strRet;
	}
}