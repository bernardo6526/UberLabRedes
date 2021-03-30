
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;

public class Usuario {

    // M�TODO PRINCIPAL DA CLASSE
	public static void main(String args[]) {
		try {

            int idClient= (int)(Math.random() * (10000 - 1 + 1) + 1);
            String tipoUser = "usuario";
            boolean ocupado = false;
            
            //System.out.println("sou o client: "+idClient);
			Scanner sc = new Scanner(System.in); // Create a Scanner object
			int comando;
			boolean exit = false;
			System.out.println("Iniciando o Uber usuário");

			while (!exit) {
				System.out.println("0-Sair\n1-Chamar Uber\n");
				comando = sc.nextInt();
				switch (comando) {
				case 0:
					exit = true;
					break;
				case 1:
					conexao(idClient,tipoUser,ocupado);
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

	public static void conexao(int idClient, String tipoUser,boolean ocupado) throws Exception {
        System.out.println("Procurando motorista...");
		
		// ENDEREÇO DO SERVIDOR
		String IPServidor = "25.19.211.24";
		int PortaServidor = 6000;
		int PortaCliente = 6001;

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
	}
}