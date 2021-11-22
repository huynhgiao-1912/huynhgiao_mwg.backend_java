package mwg.wb.client.queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.GetResponse;

public class QueueRabbitMQBeta {
	ConnectionFactory factory = null;
	private Connection connection = null;
	private Channel channel = null;
	DefaultConsumer consumer = null;
	private String ConnecttionString = "amqp://beta:beta@10.1.6.139:5672";
//	private Object _locker = new Object();
//	private boolean isConsumerError = false;

	public void Init(String connecttionString) {
		ConnecttionString = connecttionString;
	}

	public boolean Connect() {
		try {
			factory = new ConnectionFactory();
			factory.setUri(ConnecttionString);
			connection = factory.newConnection();
			channel = connection.createChannel();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		 
	}

	public boolean StartDequeue(String queue, Map<String, Object> args) throws IOException {
		if (channel != null && channel.isOpen()) {

			channel.queueDeclare(queue, true, false, false, args);
			channel.basicQos(0, 1, false);
			return true;
			// DefaultConsumer consumer = new DefaultConsumer(channel);
		}
		return false;
	}

	// ???
	public boolean StartDequeue(String queue) throws IOException {
		if (channel != null && channel.isOpen()) {
			Map<String, Object> args = new HashMap<String, Object>();
		//	args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			channel.queueDeclare(queue, true, false, false, args);
			channel.basicQos(0, 1, false);
			return true;
			// DefaultConsumer consumer = new DefaultConsumer(channel);
		}
		return false;
	}

	public QueueMesssageEventArgs Dequeue(String queue) throws IOException {
		boolean noAck = false;
		if (channel != null && channel.isOpen()) {
			GetResponse result = channel.basicGet(queue, noAck);
			if (result == null) {

			} else {

				byte[] body = result.getBody();
				String message = new String(body, "UTF-8");
				QueueMesssageEventArgs rs = new QueueMesssageEventArgs();
				rs.Tag = result.getEnvelope().getDeliveryTag();
				rs.Message = message;
				return rs;

			}
		}
		return null;

	}

	public boolean SendMessageExchange(String ExChangeName, String message) throws IOException {
		if (channel != null && channel.isOpen()) {

			// channel.exchangeDeclare(ExChangeName, "fanout");
			channel.basicPublish(ExChangeName, "", null, message.getBytes("UTF-8"));
			return true;
		}
		return false;

	}

	public boolean SendMessage(String queue, String message) throws IOException {
		if (channel != null && channel.isOpen()) {
			Map<String, Object> args = new HashMap<String, Object>();
			//args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			channel.queueDeclare(queue, true, false, false, args);
			byte[] body = message.getBytes("UTF-8");
			channel.basicPublish("", queue, new AMQP.BasicProperties.Builder().build(), body);
			return true;
		}
		return false;

	}

	public boolean SendMessagePriority(String queue, String message, int pro) throws IOException {
		if (channel != null && channel.isOpen()) {
			Map<String, Object> args = new HashMap<String, Object>();
			//args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			channel.queueDeclare(queue, true, false, false, args);
			byte[] body = message.getBytes("UTF-8");
			AMQP.BasicProperties.Builder basicProps = new AMQP.BasicProperties.Builder();
			basicProps.priority(pro);

			channel.basicPublish("", queue, basicProps.build(), body);
			return true;
		}
		return false;

	}

	public boolean Ack(long DeliveryTag) throws IOException {

		if (channel != null && channel.isOpen()) {
			channel.basicAck(DeliveryTag, false);
			return true;
		}

		return false;
	}

	public int Count(String queue) {
		try {
			if (channel != null && channel.isOpen()) {
//				Map<String, Object> arguments = new HashMap<String, Object>();
				// arguments.Add("x-max-priority", 100);
				DeclareOk result = channel.queueDeclare(queue, true, false, false, null

				);
				return result.getMessageCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean Nack(long DeliveryTag) throws IOException {

		if (channel != null && channel.isOpen()) {
			channel.basicNack(DeliveryTag, false, false);
			return true;
		}

		return false;
	}

	/// <returns></returns>
	public boolean Close() throws IOException, TimeoutException {
		try {
			//channel.abort();
			//connection.abort();

			if (connection.isOpen())
				connection.close();
			if (channel.isOpen())
				channel.close();

			channel = null;
			connection = null;
			factory = null;
			consumer = null;

		} catch (Exception e) {
			//e.printStackTrace();
		}

		return true;
	}

}
