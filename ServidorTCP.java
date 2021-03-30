import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;

public class ServidorTCP
{
	//MÉTODO PRINCIPAL DA CLASSE
	public static void main (String args[])
	{
		try
		{
			//for(int i=0; i<3;i++){
				int PortaServidor = 7000;
				
				//INICIALIZA UM SERVIÇO DE ESCUTA POR CONEXÕES NA PORTA ESPECIFICADA
				System.out.println(" -S- Aguardando conexao (P:"+PortaServidor+")...");
				ServerSocket socktServ = new ServerSocket(PortaServidor);
				
				System.out.println(" -S- Aguardando conexao (P:"+PortaServidor+")...");
				ServerSocket socktServ1 = new ServerSocket(PortaServidor);
				//----------------
				
				//ESPERA (BLOQUEADO) POR CONEXÕES			
				Socket conSer = socktServ.accept(); //RECEBE CONEXÃO E CRIA UM NOVO CANAL (p) NO SENTIDO CONTRÁRIO (SERVIDOR -> CLIENTE)
				System.out.println(" -S- Conectado ao cliente ->" + conSer.toString());
				
				Socket conSer1 = socktServ1.accept(); //RECEBE CONEXÃO E CRIA UM NOVO CANAL (p) NO SENTIDO CONTRÁRIO (SERVIDOR -> CLIENTE)
				System.out.println(" -S- Conectado ao cliente ->" + conSer1.toString());
				//----------------
			
				//CRIA UM PACOTE DE ENTRADA PARA RECEBER MENSAGENS, ASSOCIADO À CONEXÃO (p)
				ObjectInputStream sServIn = new ObjectInputStream(conSer.getInputStream());
				System.out.println(" -S- Recebendo mensagem...");
				Object msgIn = sServIn.readObject(); //ESPERA (BLOQUEADO) POR UM PACOTE
				System.out.println(" -S- Recebido: " + msgIn.toString());
				
				ObjectInputStream sServIn1 = new ObjectInputStream(conSer1.getInputStream());
				System.out.println(" -S- Recebendo mensagem...");
				Object msgIn1 = sServIn1.readObject(); //ESPERA (BLOQUEADO) POR UM PACOTE
				System.out.println(" -S- Recebido: " + msgIn1.toString());
				
				//CRIA UM PACOTE DE SAÍDA PARA ENVIAR MENSAGENS, ASSOCIANDO-O À CONEXÃO (p)
				ObjectOutputStream sSerOut = new ObjectOutputStream(conSer.getOutputStream());
				sSerOut.writeObject("RETORNO " + msgIn.toString() + " - TCP"); //ESCREVE NO PACOTE
				System.out.println(" -S- Enviando mensagem resposta...");
				sSerOut.flush(); //ENVIA O PACOTE
				
				ObjectOutputStream sSerOut1 = new ObjectOutputStream(conSer1.getOutputStream());
				sSerOut1.writeObject("RETORNO " + msgIn1.toString() + " - TCP"); //ESCREVE NO PACOTE
				System.out.println(" -S- Enviando mensagem resposta...");
				sSerOut1.flush(); //ENVIA O PACOTE
			
				//FINALIZA A CONEXÃO
				socktServ.close();
				conSer.close();
				System.out.println(" -S- Conexao finalizada...");
				
				socktServ1.close();
				conSer1.close();
				System.out.println(" -S- Conexao finalizada...");
			//}	

		}
		catch(Exception e) //SE OCORRER ALGUMA EXCESSÃO, ENTÃO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println(" -S- O seguinte problema ocorreu : \n" + e.toString());
		}
	}
}