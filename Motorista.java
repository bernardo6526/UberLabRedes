
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Motorista {
	// atributos do motorista
	static int idClient = (int) (Math.random() * (10000 - 1 + 1) + 1);
	static String tipoUser = "motorista";
	static boolean ocupado = false;
	static double latitude = (Math.random() * (20000 - 1 + 1) + 1);
	static double longitude = (Math.random() * (20000 - 1 + 1) + 1);
	static String nome = "Bruce Banner";

	// infos para conectar ao servidor
	static String IPServidor = "25.19.211.24"; // ENDEREÇO DO SERVIDOR
	static int PortaServidor = 6000; // define qual porta do servidor será acessada
	static int PortaCliente = 6002; // define qual porta será usada para se comunicar com o Servidor

	// portas de conexao P2P
	static int PortaUsuario = 6003; // define qual porta do cliente do Usuario será acessada
	static int PortaMotorista = 6004;// define qual porta será usada para se comunicar com o Usuario

	static Scanner sc = new Scanner(System.in); // Create a Scanner object
	// MÉTODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {
			// System.out.println("sou o client: "+idClient);
			boolean exit = false;
			System.out.println("Iniciando o Uber " + tipoUser);

			while (!exit) {
				System.out.println("0-Sair\n1-Procurar Corrida\n");
				int comando = -1;
				comando = sc.nextInt();
				switch (comando) {
				case 0:
					exit = true;
					break;
				case 1:
					int newPort = conexaoInicial(idClient, tipoUser, ocupado, latitude, longitude, nome);
					conexao(idClient, tipoUser, ocupado, latitude, longitude, nome, newPort);
					break;
				default:
					System.out.println("comando não reconhecido");

				}
			}
			sc.close();
		} catch (Exception e) { // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
			System.out.println("-C- O seguinte problema ocorreu na main : \n" + e.toString());
		}
	}

	public static int conexaoInicial(int idClient, String tipoUser, boolean ocupado, double latitude, double longitude,
			String nome) throws Exception {
		System.out.println("Conectando ao servidor Uber...");

		// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
		DatagramSocket ds = new DatagramSocket(PortaCliente);
		// System.out.println("-C- Cliente estabelecendo servico UDP (P" + PortaCliente
		// + ")...");

		// CRIA UM PACOTE E ENVIA PARA O SERVIDOR
		String strEnvio = idClient + "/" + tipoUser + "/" + ocupado + "/" + latitude + "/" + longitude + "/" + nome;
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
		// System.out.println("DEBUG |" + strRet+"|");
		strRet = strRet.trim(); // remove os espaços em branco da string recebida
		int newPort = Integer.parseInt(strRet);

		// FINALIZA O SERVIÇO UDP
		ds.close();
		// System.out.println("-C- Conexao Inicial finalizada...");

		return newPort; // retorna o valor da porta
	}

	public static void conexao(int idClient, String tipoUser, boolean ocupado, double latitude, double longitude,
			String nome, int newPort) throws Exception {
		System.out.println("Procurando usuario...");

		// Define qual será a nova porta de acesso do servidor
		int PortaServidor = newPort;

		// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
		DatagramSocket ds = new DatagramSocket(PortaCliente);
		// System.out.println("-C- Cliente estabelecendo servico UDP (P" + PortaCliente
		// + ")...");

		// CRIA UM PACOTE E ENVIA PARA O SERVIDOR
		String strEnvio = idClient + "/" + tipoUser + "/" + ocupado + "/" + latitude + "/" + longitude + "/" + nome;
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(IPServidor),
				PortaServidor);
		// System.out.println("-C- Enviando mensagem (IP:" + IPServidor + " - P:" +
		// PortaServidor + ")...:" + strEnvio);
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
		// System.out.println("-C- Mensagem recebida: " + strRet);
		String[] usuario = strRet.split("/");

		// Conecta ao usuario
		corrida(usuario);

		// FINALIZA O SERVIÇO UDP
		ds.close();
		// System.out.println("-C- Conexao finalizada...");

	}

	// Realiza a conexao com o usuario durante a corrida
	// info 0idClient,1tipoUser,2ocupado,3latitude,4longitude,5nome
	// 6latDestino,7longDestino,8IpCliente,9PortaCliente
	public static void corrida(String[] info) {
		try {
			String nome = info[5];

			// define a localizao atual do usuario
			double latUsuario = Double.parseDouble(info[3]);
			double longUsuario = Double.parseDouble(info[4]);

			// define o endereço de destino que o usuario deseja ser levado
			double latDestino = Double.parseDouble(info[6]);
			double longDestino = Double.parseDouble(info[7]);

			String ipUsuario = info[8];
			// System.out.println("ipUsuario: "+ipUsuario);

			while (true) {
				sc.nextLine(); // desbugando o scan
				System.out.println("Aceitar a corrida de " + nome + "? \n y - aceitar , n - recusar");
				String accept = sc.nextLine();
				if (accept.equals("n")) {
					System.out.println("Corrida recusada.");
					break;
				}
				System.out.println(
						"Dirija-se para a localizacao de " + nome + ": (" + latUsuario + "," + longUsuario + ")");

				// ESTABELECE UM SERVIÇO UDP NA PORTA ESPECIFICADA
				DatagramSocket ds = new DatagramSocket(PortaMotorista);
				// System.out.println("-C- Cliente estabelecendo servico UDP (P" +
				// PortaMotorista + ")...");

				double distanciaUsuario = calcDist(latUsuario, longUsuario);
				while (distanciaUsuario > 2) {
					double velocidade = (Math.random() * (80 - 30 + 1) + 30);
					int tempo = (int) (distanciaUsuario / velocidade);
					if (tempo == 0)
						break;
					System.out.println("Voce esta a " + tempo + " min de distancia de " + nome);
					enviarPacote("O motorista esta a " + tempo + " min de distancia", ipUsuario, ds);
					Thread.sleep(400);
					distanciaUsuario /= 2;
				}

				enviarPacote("O motorista chegou", ipUsuario, ds);
				Thread.sleep(400);
				enviarPacote("Iniciando viagem", ipUsuario, ds);
				Thread.sleep(400);

				System.out
						.println("Dirija-se para o destino de " + nome + ": (" + latDestino + "," + longDestino + ")");

				double preco = 0;
				double distanciaDestino = calcDist(latDestino, longDestino);
				while (distanciaDestino > 2) {
					double velocidade = (Math.random() * (80 - 30 + 1) + 30);
					int tempo = (int) (distanciaDestino / velocidade);
					if (tempo == 0)
						break;
					System.out.println("Voce esta a " + tempo + " min de distancia do destino de " + nome);
					enviarPacote("O seu destino esta a " + tempo + " min de distancia", ipUsuario, ds);
					Thread.sleep(400);
					distanciaDestino /= 2;
					preco += 4.59;
				}

				System.out.println("Preco da viagem: R$" + String.format("%.2f", preco));
				Thread.sleep(400);
				enviarPacote("Pague R$" + String.format("%.2f", preco) + " ao motorista", ipUsuario, ds);
				Thread.sleep(400);
				enviarPacote("Viagem encerrada", ipUsuario, ds);
				receberPacote(ds);

				ds.close();
				break;
			}
		} catch (Exception e) { // SE OCORRER ALGUMA EXCESSAO, ENTAO DEVE SER TRATADA (AMIGAVELMENTE)
			System.out.println("-C- O seguinte problema ocorreu na conexao da corrida : \n" + e.toString());
		}
	}

	public static void enviarPacote(String strEnvio, String ipDestino, DatagramSocket ds) throws Exception {
		// CRIA UM PACOTE E ENVIA PARA O USUARIO
		byte[] bytEnvio = strEnvio.getBytes();
		DatagramPacket pktEnvio = new DatagramPacket(bytEnvio, bytEnvio.length, InetAddress.getByName(ipDestino),
				PortaUsuario);
		// System.out.println("-C- Enviando mensagem (IP:" + IPServidor + " - P:" +
		// PortaUsuario + ")...:" + strEnvio);
		ds.send(pktEnvio);
	}

	public static String receberPacote(DatagramSocket ds) throws Exception {
		// CRIA UM PACOTE E RECEBE DADOS DO SERVIDOR
		byte[] bytRec = new byte[100];
		DatagramPacket pktRec = new DatagramPacket(bytRec, bytRec.length);
		// System.out.println("-C- Recebendo mensagem...");
		ds.receive(pktRec);
		bytRec = pktRec.getData();
		String strRet = new String(bytRec, 0, bytRec.length);
		strRet = strRet.trim();
		System.out.println(strRet);
		return strRet;
	}

	// Calcula a distancia entre usuario e motorista
	public static double calcDist(double latDestino, double longDestino) {
		double x1 = latDestino;
		double y1 = longDestino;
		double x2 = latitude;
		double y2 = longitude;
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
}