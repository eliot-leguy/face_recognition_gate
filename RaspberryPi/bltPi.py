import bluetooth
import os
import cv2

PATH = "Path_to_Images"

server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server_socket.bind(("", bluetooth.PORT_ANY))
server_socket.listen(1)

port = server_socket.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

bluetooth.advertise_service(server_socket, "Echo Server", service_id = uuid, service_classes = [uuid, bluetooth.SERIAL_PORT_CLASS], profiles = [bluetooth.SERIAL_PORT_PROFILE])

def wait_for_connection():
	print("Waiting for connection in RFCOMM channel %d" % port)

	client_socket, client_info = server_socket.accept()
	print("Accepted connection from ", client_info)
	listen_for_data(client_socket)

def listen_for_data(client_socket):
	ImagesToSend = []
	while True:
		try:
			bytes_data = client_socket.recv(1024)
			#Peut-être check combien de bytes faut pour faire un caractère ?
			dataString = bytes_data.decode("utf-8")
			if(dataString[0] == '*'):
				ImagesToSend = matchUsers(client_socket,dataString[1:])
			if(dataString[0] == '1'):
				if(len(ImagesToSend)>0):
					sendImage(client_socket,ImagesToSend[0])





			# print("Received: %s" % data)
			# client_socket.send(data)

		except IOError:
			print("Client has disconnected")
			wait_for_connection()
			break

		except KeyboardInterrupt:
			print("Shutting down socket")
			server_socket.close()
			client_socket.close()


def matchUsers(client_socket,dataString):
	namesPhone = []
	name = ""
	for car in dataString:
		if(car == '*'):
			namesPhone.append(name)
			name=""
		else:
			name += car
	if(len(name)>0):
		namesPhone.append(name)

	namesPi = deleteExtensions(os.listdir(PATH))

	imagesToSend = []
	for namePi in namesPi:
		SameName = False
		for namePhone in namesPhone:
			if(namePi == namePhone):
				SameName = True
				break
		if(not(SameName)):
			imagesToSend.append(namePi)

	bytes_NbImagesToSend = str(len(imagesToSend)).encode()
	client_socket.send(bytes_NbImagesToSend)

	#Now we wait for a 1 from the phone to start sending the missing users
	dataString = ""
	while dataString != '1':
		bytes_data = client_socket.recv(1024)
		dataString = bytes_data.decode("utf-8")
	
	for imageName in imagesToSend:
		sendImage(client_socket, imageName)
	




def deleteExtensions(names):
	newNames = []
	for name in names:
		newName = ""
		for car in name:
			if(car=="."):
				newNames.append(newName)
				break
			else:
				newName += car
	return newNames

def sendImage(client_socket,imageName):

	#Sending the name of the image :
	bytes_nameToSend = imageName.encode()
	client_socket.send(bytes_nameToSend)

	img = cv2.imread(PATH + imageName + ".png")

	#Sending the size of the image :
	imgSize = img.shape[0] * img.shape[1] * img.shape[2]
	bytes_SizeImageToSend = str(imgSize).encode()
	client_socket.send(bytes_SizeImageToSend)



	b_img = cv2.imencode('.jpg', img)[1].tobytes()


wait_for_connection()