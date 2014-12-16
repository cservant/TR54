#Rapport de Projet TR54

##Sommaire
TODO

##Description du sujet
Dans le domaine de la robotique, les intersections sont gérées soit à travers la planification à priori, soit en dotant les robots d’une certaine autonomie. Dans le cadre de cette dernière approche, les robots disposent de capteurs pour éviter les obstacles. Ainsi, à l’approche des intersections, les robots doivent ralentir ou s’arrêter en fonction de leur champ de détection pour éviter les collisions. La communication sans-fil et le positionnement peuvent contribuer à améliorer les performances des intersections dans la mesure où la portée du champ de détection est étendue à la portée de la communication sans-fil, ce qu’on appelle système coopératif d’évitement d’obstacle. Il est possible de gagner en performance si le système conçu prévoie une signalisation personnalisée, à savoir un protocole de négociation des droits de passages. Cette négociation est abordée à travers des architectures centralisées et décentralisées. Nous retiendrons par la suite l’architecture centralisée car d’une part, la technologie EV3 est limitée à l’architecture centralisée et d’autre part, la décentralisation du protocole comporte des risques de collisions. L’architecture centralisée est nommé « intersection autonome ».

L’objectif du projet est de créer une maquette d’intersection autonome. Il s’agit d’une intersection régulée à travers la communication sans-fil. La maquette est constituée d’une intersection, d'au moins trois véhicules et d’un régulateur représenté par notre mobile Android. Les robots doivent pouvoir effectuer plusieurs tours, sans collision ni interblocage.

##Circuit
![Circuit](https://github.com/cservant/crossroad-synchronisation/blob/master/pictures/circuit.png)

Le circuit est tracé à l'aide de rubans adhésifs de couleurs noir, bleu et orange et d'un fond blanc. Celui-ci a la forme d'un huit. L’unique intersection est constituée de deux voies qui se croisent et qui seront appelées « voie 1 » et « voie 2 ». Chaque voie implique un mouvement rectiligne constant. De plus, chaque mouvement doit rencontrer trois zones : une zone de stockage, une zone de conflit puis une zone de sortie. En dehors des zones de stockage et de la zone de conflit, les robots se trouvent en « voie 0 » dans laquelle aucun processus de négociation n’est en cours.

##Robot LEGO MINDSTORMS EV3
Pour ce projet nous avons à disposition des robots **LEGO MINDSTORMS EV3** et leurs briques de construction composés de :

- La brique programmable.
- Deux Grands moteurs.
- Un capteur de couleur.
- Un capteur de pression.
- Un capteur à ultrasons.
- De briques de constructions pour assembler le robot.

##Mobile Android
Le mobile Android est présent pour faire office de point d'accès Wifi auprès duquel vont se connecter les robots. Les adresses sont assignées automatiquement par le DHCP du mobile. 
Une application a été créée pour récupérer les demandes des robots et gérer les autorisations d'accès à l'intersection.

##Environnement de développement
Pour mener à bien nos développements, nous travaillons avec l'IDE Eclipse et un plugin **Lejos** qui nous fournit la partie *HAL* pour programmer les robots ainsi qu'une interface de connection a ceux ci.

##Partie Véhicule Embarqué
###Politique de suivi
La politique de suivi utilisée est celle du tout ou rien. Tant que le robot ne rencontre aucun obstacle à une certaine distance, il continue sa progression. Si un obstacle survient dans sa zone de détection, le robot s'arrêtera alors et attendra que la route soit dégagée. 

###Détection de couleur
Lors de nos différentes expérimentations sur différents circuits, deux méthodes sont ressorties pour détecter les lignes :

- détection par couleur en utilisant les constantes de couleur Lejos.
- détection par luminance.

La première méthode - la plus simple - permet un déploiement rapide et une détection globale des couleurs del'environnement. Néanmoins cette méthode est très sujette aux erreurs dues aux reflets sur les lignes ou encore à l'éclairage de la pièce.

En ce qui concerne la seconde méthode, celle ci est bien plus fiable mais nécessite plusieurs tests expérimentaux pour parvenir à définir les luminances correspondants aux couleurs utilisés sur les lignes. Après cela, cette méthode est la plus fiable puisque l'environnement en changeant pas en salle de TP, les luminances non plus.
Malheureusement cette méthode n'est valable que dans un environnement précis et ce une fois les valeurs réglées pour celui ci.

En conséquences nous avons choisi de garder la méthode par détection de couleur pour assurer le suivi de ligne. Afin de minimiser les défauts dûs à l'éclairage et aux reflets, nous avons également fait le choix d'appliquer deux vitesse différentes au robot:	

- une vitesse de croisière pour les voies principales
- une vitesse d'adaptation pour les virages dans lesquels la capteur de couleur pose problème.

###Communication Wifi
Les robots communiquent via Wifi directement avec le *serveur Android*. Quand un robot arrive sur la ligne droite du croisement il envoie une requète d'autorisation au serveur. 
A tout moment le robot tient à jour sa position sur le circut. Il sait quand il arrive sur l'intersection et sur quelle voie il se trouve (voie 1 ou voie 2).

###Détection de l'intersection
Afin de détecter l'intersection et donc de permettre de gérer les croisements des robots, nous utilisons l'odométrie. Grâce à l'odométrie, nous sauvegardons le point d'entrée dans la voie principale (menant à l'intersection) et nous calculons ensuite la distance parcourue depuis ce point. Dès que cette distance nous approche du point de l'intersection, nous considérons alors que nous entrons dans la zone de stockage et que nous sommes en attente de l'autorisation du serveur.

Dès l'autorisation du serveur reçue, le robot peut alors continuer sa progression dans la zone de conflit.
Encore une fois grâce à l'odométrie, nous détectons la distance parcourue depuis l'intersection pour savoir quand le robot est sorti de la zone de conflit. Le robot entre en zone de sortie. Celui ci envoie alors une notification au serveur pour lui signaler qu'il a libéré l'intersection.

##Partie Serveur
###Interface
L'interface de l'application est très simple. L'application affiche l'IP et le port sur lequel on créé les sockets. De plus, elle affiche les messages envoyés et reçus (adresse des recepteurs et des emetteurs). A chaque reception d'un message, le thread de l'activité courante est utilisé pour actualiser l'affichage :

    MainActivity.this.runOnUiThread(new Runnable() {
    	@Override
    	public void run() {
    		info.setText("I'm waiting here: " + serverSocket.getLocalPort());
    	}
    });

###Ordonnancement
Le serveur gère les requêtes par séquence. La première arrivée est la première répondue. Cependant toutes les requêtes sont traitées. Deux listes sont maintenues sur le serveur. Une liste des robots autorisés et une liste contenant les robots en attente d'autorisation. (Donc ceux 

Lorsqu'un robot demande l'accès à l'intersection, le serveur regarde si cette dernière est libre. Si elle est libre, le  robot reçoit l'autorisation.

##Problèmes rencontrés 
- perte de temps pour charger les \*.jar sur les EV3 lorsque le Wi-Fi est actif. Solution: utiliser la commande **pscp -scp <nom du .jar> root@<robot ip>:/home/lejos/programs**.
- lorsque le circuit est bombé ou plié, la piste est alors trop proche du capteur et la couleur detectée est faussée ce qui engendre un problème de suivi de ligne.
- lors du chargement des fichiers sur le robot, il est arrivé que les fichiers soient corrompus et deviennent inaccessible sur le robot. ceux ci sont alors inexploitables et impossible à supprimer/remplacer.	
Solution: générer un nouvel exécutable avec un nom différent de ceux sur le robot.
- nombreux redémarrages des robots dûs à un échec de chargement (perte de temps).

##Conclusion
Ce projet nous a permi de découvrir l'environnement **Lejos**. Nous avons été sensibilisé aux problématiques d'ordonnancement et d'interblocages. 
Il nous a permi de réfléchir et de tester de nouvelles pôlitiques de suivi et de synchronisation des véhicules pour optimiser les temps de croisement et de parcours sans pour autant négliger la sécurité.

##Auteurs
- Alexis Hestin
- Stéphane Dorre
- Julien Voisin
- Clément Servant

