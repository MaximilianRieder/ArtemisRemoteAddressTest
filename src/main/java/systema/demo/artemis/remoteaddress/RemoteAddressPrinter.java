package main.java.systema.demo.artemis.remoteaddress;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnection;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.remoting.Connection;

/**
 * @author Maximilian Rieder
 */
public class RemoteAddressPrinter
{
	private static final String BROKER_URL = "(tcp://hostA:6666,tcp://hostB:6666)?failoverAttempts=-1";
	private static final int CHECK_INTERVAL = 5;

	public static void main(String[] args)
	{
		String brokerUrl;
		if ( args.length > 0 )
		{
			brokerUrl = args[0];
		}
		else
		{
			brokerUrl = BROKER_URL;
		}
		try ( ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
		      ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection() )
		{
			connection.start();
			try ( ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor() )
			{
				scheduler.scheduleAtFixedRate(() -> printRemoteConnection(connection), 0, CHECK_INTERVAL,
				                              TimeUnit.SECONDS);
				// Keep alive
				Thread.currentThread().join();
			}
		}
		catch ( Exception e )
		{
			System.err.println("Connection error: " + e.getMessage());
		}
	}

	private static void printRemoteConnection(ActiveMQConnection connection)
	{
		try
		{
			ClientSessionFactory sessionFactory = connection.getSessionFactory();
			RemotingConnection remotingConnection = sessionFactory.getConnection();
			Connection transportConnection = remotingConnection.getTransportConnection();
			String remoteAddress = transportConnection.getRemoteAddress();
			System.out.println("Remote address: " + remoteAddress);
		}
		catch ( Exception e )
		{
			System.err.println("Error retrieving broker information: " + e.getMessage());
		}
	}
}
