package mwg.wb.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
//import com.sun.media.jfxmedia.track.Track.Encoding;

public class QueueRabbitMQ {
	ConnectionFactory factory = null;
	private Connection connection = null;
	private Channel channel = null;
	DefaultConsumer consumer = null;
	private String ConnecttionString = "";
	private Object _locker = new Object();
	private boolean isConsumerError = false;

	public void Init(String connecttionString) {
		ConnecttionString = connecttionString;
	}

	// ???
	public boolean StartSubscribe(String exchange, String type) throws IOException {
		if (channel != null && channel.isOpen()) {
			channel.exchangeDeclare(exchange, type, true);

			String queueName = channel.queueDeclare().protocolMethodName();
			channel.queueBind(queueName, exchange, "");

			System.out.println(" [*] Waiting for logs.");

			consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) {

					try {
						String message = new String(body);

//		                MessageReceived?.Invoke(this, new QueueMesssageEventArgs()
//		                {
//		                    Tag = ea.DeliveryTag,
//		                    Message = message
//		                });
					} catch (Exception ex) {
//		                unRegisterHandlers();
//		                cancelRun();
					}
				}
			};
			channel.basicConsume(queueName, true, consumer);
			return true;
		}
		return false;
	}

	// ??
	public void Dequeue(String queue) {
		if (channel != null && channel.isOpen()) {
			try {
				channel.queueDeclare(queue, true, false, false, null);

				channel.basicQos(0, 1, false);
				DefaultConsumer consumer = new DefaultConsumer(channel);

				boolean noAck = false;
				GetResponse result = channel.basicGet(queue, noAck);
				if (result == null) {
					// No message available at this time.
				} else {
					BasicProperties props = result.getProps();
					byte[] body = result.getBody();
					String message = new String(body, "UTF-8");
//					 return new QueueMesssageEventArgs()
//			                {
//			                    Tag = result.DeliveryTag,
//			                    Message = message
//			                };

				}
			} catch (Exception e) {
				return; // null;
			}
		}
		return; // null;
	}

}
