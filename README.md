Nowadays drivers are the weakness of our road safety policy. To improve our road safety we work on a well known solution : autonomous vehicles.
The problem raised by this solution is "How to syncronize the different vehicles to optimize the time inside the crossroads and have the best traffic ?"

There are many possibilities to answer this quesiton. The three main ones are :
- Time synchronisation in terms of roads (principle of our traffic lights)
- Sequential synchronisaiton (we attribute resource to vehicles instead of roads)
- One by One (each vehicle is communicating with all its neighbors and negociating its own authorization)

The project is divided in 2 parts : 
- the vehicle part which is in charge of keeping the vehicle on its road and managing the close range hazards detection
- the controller part which is in charge of managing the authorizations for the vehicles to cross the intersections

We work on LEGO MINDSTORMS EV3 robots with a lejos API on Java for the vehicle part and we use an android device as a server for the controller part.

The EV3 robots use WiFi to communicate with the controller.
They use ultrasound sensor to perform close range detection and a color/light sensor to perform the road detection.


This project has been created for the lecture TR54 - Real Time commands. This lecture is provided by the [UTBM](http://www.utbm.fr/).

Stéphane Dorre
Alexis Hestin
Voisin Julien
Servant Clément
