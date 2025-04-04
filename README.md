## Test procedure:
- Configure a cluster with two Artemis brokers with high availability.
In my tests the acceptors and connectors were configured like this.
Configuration broker hostA:
```
<acceptors>
    <acceptor name="client">tcp://0.0.0.0:6666?protocols=CORE,AMQP;tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;useEpoll=true;supportAdvisory=false;suppressInternalManagementObjects=true;ha=true;failoverAttempts=-1</acceptor>
    <acceptor name="cluster">tcp://0.0.0.0:6000?protocols=CORE;tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;useEpoll=true</acceptor>
</acceptors>
<connectors>
    <connector name="net-hostA">tcp://hostA:6000</connector>
    <connector name="net-hostB">tcp://hostB:6000</connector>
</connectors>
<cluster-connections>
    <cluster-connection name="test-net">
        <connector-ref>net-hostA</connector-ref>
        <static-connectors>
            <connector-ref>net-hostB</connector-ref>
        </static-connectors>
    </cluster-connection>
</cluster-connections>
```
Configuration broker hostB:
```
<acceptors>
    <acceptor name="client">tcp://0.0.0.0:6666?protocols=CORE,AMQP;tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;useEpoll=true;supportAdvisory=false;suppressInternalManagementObjects=true;ha=true;failoverAttempts=-1</acceptor>
    <acceptor name="cluster">tcp://0.0.0.0:6000?protocols=CORE;tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;useEpoll=true</acceptor>
</acceptors>
<connectors>
    <connector name="net-hostA">tcp://hostA:6000</connector>
    <connector name="net-hostB">tcp://hostB:6000</connector>
</connectors>
<cluster-connections>
    <cluster-connection name="test-net">
        <connector-ref>net-hostB</connector-ref>
        <static-connectors>
            <connector-ref>net-hostA</connector-ref>
        </static-connectors>
    </cluster-connection>
</cluster-connections>
```
- Set a broker URL either in your IDE in RemoteAddressPrinter.BROKER_URL or as the first command line argument. 
  - In this case I used: (tcp://hostA:6666,tcp://hostB:6666)?failoverAttempts=-1
- Start RemoteAddressPrinter.main(). This will periodically print the remoteAddress derived through:
```
ClientSessionFactory sessionFactory = connection.getSessionFactory();
RemotingConnection remotingConnection = sessionFactory.getConnection();
Connection transportConnection = remotingConnection.getTransportConnection();
String remoteAddress = transportConnection.getRemoteAddress();
```
- The output will be something like: hostB/192.123.123.123:6666 (using the client port)
- Shut down the host where the application is currently connected to. This will trigger a failover.
- After the failover the application will reconnect.
- Now the remote address points to the other host, but specifies the cluster port, e.g.: hostA/192.168.123.2:6000
- Main question: Is it intended that the remote address is pointing to the port/acceptor that is used by the cluster connection?