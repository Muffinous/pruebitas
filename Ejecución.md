# Adaptación y ejecución del programa JRoar.

La última modificación de JRoar fue en 2002 y por lo tanto, el java que se usaba en esos años era el 8.
Actualmente nosotros tenemos la openjdk version "11.0.6" y por lo tanto, el programa no ejecuta.   

Para evitar tener que cambiar la version de java, decidimos actualizar el proyecto de manera que pudiese runear, los pasos son los siguientes:

> Crear un nuevo proyecto java desde cero.  

> Copiar las carpetas **com.jcraft** y **misc** en la carpeta src de nuesto nuevo proyecto. 
 
> Descargarnos una canción que queramos y convertirla en .ogg (*Existen convertidores online para esto*).  
 
> Crear una carpeta para que podamos tener esa canción en el proyecto, por ejemplo una carpeta llamada 'res' y dentro 'songs'.     

> Ir a 'editar configuraciónes..' y añadir en los *argumentos del programa*: **-port 8000 -playlist /testing.ogg ./././res/songs/LonelyEyes.ogg**.
>> El puerto puede ser el que queramos, no tiene por qué ser 8000.  
>> LonelyEyes.ogg es la canción que nos descargamos.  
>> testing.ogg también puede tener el nombre que queramos, pero si tiene que tener .ogg al final.

> Finalmente, podemos ejecutar el proyecto desde la clase JRoar y acceder en nuestro navegador a localhost:8000. (o el puerto que hayamos puesto).  
 
> Si tiene VLC puede ir a stream puede escuchar la canción yendo a: 'Media -> Open Network Stream -> Poner en la URL: 'http://localhost:8000/testing.ogg'.