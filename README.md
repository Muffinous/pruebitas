# Mantenimiento

### Modificaciones de código.
En este apartado explicaremos las modificaciones que hemos ido haciendo en el código.  
  
Respecto a los imports:   
En varias clases existía un '**import java.lang.**'. Hemos borrado este import ya que actualmente, todas las clases del
paquete java.lang están importadas por defecto.  
Además, en varias clases existían imports que no se usaban en ningún momento por lo que tambíen los 
hemos eliminado.  
 
  
Carpeta MISC :file_folder:	  
En esta carpeta podemos encontrar clases relacionadas con las canciones que estén en el buffer.  
En la clase Playlist, existe un bucle for el cual solamente tiene una condición para iterar, es decir, 
no existe ni inicialización de variables ni incremento, por ello decidimos cambiarlo a un **bucle while**.   
El resultado sería eliminar los puntos y coma que existen (;) y dejar la condición como while(keys.hasMoreElements()).

    for(; keys.hasMoreElements();){
      String mountpoint=((String)(keys.nextElement()));
      try{
        s.println(mountpoint);
      }
      catch(Exception e){
        //s.println(e.toString());
      }
      
Respecto a la clase RadioStudio, en varias ocasiones se repetía el String \ \ por lo que para evitar tener tanto código 
repetido, se puede crear una variable que tenga ese valor. La variable la hemos llamado 'bars'. Además de esto, hemos 
hecho la misma modificación explicada anteriormente con los bucles for y while. 

Tanto RadioStudio.java, como PlayList.java, JOrbisPlayer.java y BlankPage.java extienden de la clase UserPage, que tiene 
el método Kick. Cuando una clase extiende de otra y usa un método de ella, es recomendable poner un @Override encima de la 
modificación de ese método para evitar tener errores de compilación por si, por ejemplo, el programador se equivoca al 
escribir el nombre. 