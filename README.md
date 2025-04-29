# 🎲 Puissance 4 avec chat

Ce dépôt contient une application pouvant se jouer à plusieurs personnes étant 
sur le même réseau ou sur le même PC si besoin.

## Installation du jeu

# Côté Serveur

- Premièrement il faut cloner ce dépôt afin de récupérer tous les fichiers nécessaires

```
git clone git@github.com:Keryannlecodeur/docker-sae203.git
```


- Ensuite il suffit de se placer dans le répertoire avec la commande cd


- On créée le conteneur docker :

```
git build -t <nom_conteneur> .
```

On remplace le <nom_conteneur> par la nom voulu

```
docker run -it -p 8080:8080 puissance4 
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
