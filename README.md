# 🎲 PuissanceChat

Ce dépôt contient une application pouvant se jouer à plusieurs personnes étant 
sur le même réseau ou sur le même PC si besoin.

[Cliquez ici pour allez sur le site de présentation du site](https://keryannlecodeur.github.io/docker-sae203/)

# Prérequis

- Avoir git installé sur sa machine

- Avoir docker installé sur sa machine


## Installation du jeu

# Côté Serveur

- Premièrement il faut cloner ce dépôt afin de récupérer tous les fichiers nécessaires

```
git clone git@github.com:Keryannlecodeur/docker-sae203.git
```


- Ensuite il suffit de se placer dans le répertoire avec la commande cd

- Pour éviter des problèmes pour le fichier start.sh ouvrir git bash et se rendre dans le bon dossier ensuite faire: 

```
dos2unix start.sh
```


- On créée le conteneur docker :

```
docker build -t <nom_conteneur> .
```

On remplace le <nom_conteneur> par la nom voulu

```
docker run -it -p 8080:8080 <nom_conteneur> 
```

Le serveur devrait désormais être démarré



# Côté Client 

###  Le client doit également cloner le dépôt puis ensuite il doit :

Compiler le fichier client  : 

```
javac ClientPuissance4IHM.java
```

Puis l'éxécuter :

```
java ClientPuissance4IHM
```
