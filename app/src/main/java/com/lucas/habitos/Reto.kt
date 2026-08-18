package com.lucas.habitos

import kotlin.random.Random

/**
 * El reto que hay que superar para abandonar una actividad antes de tiempo.
 *
 * Antes bastaba con mantener pulsado tres segundos, o escribir un PIN que uno
 * mismo se habia puesto. Las dos cosas se convierten en gesto reflejo a la
 * tercera vez: la mano aprende el atajo y la cabeza no llega a intervenir.
 *
 * Ahora hay que elegir una categoria, leer una capsula de divulgacion y acertar
 * TRES preguntas seguidas sobre detalles concretos de lo que acabas de leer.
 * Fallar una manda a una capsula nueva desde el principio.
 *
 * El gancho es una pregunta que pica —cuantos huesos tiene el cuerpo, de que
 * color eran las estatuas griegas—, pero las preguntas del final NUNCA son esa.
 * Van sobre un dato de dentro. Saber que solo queda en pie la Gran Piramide no
 * te dice cuanto medía el Coloso de Rodas.
 *
 * Sobre los datos: cada capsula lleva su fuente y las cifras que son respuesta
 * estan verificadas. Donde las fuentes discrepan, el dato no se usa como
 * pregunta. Una app que te hace leer para salir no puede enseñarte algo falso.
 */

enum class Categoria(val etiqueta: String, val icono: Int) {
    CUERPO("Cuerpo humano", R.drawable.ic_h_corazon),
    ESPACIO("Astronomía", R.drawable.ic_h_sol),
    HISTORIA("Historia", R.drawable.ic_h_leer),
    ARTE("Arte", R.drawable.ic_h_escribir),
    ANIMALES("Animales", R.drawable.ic_h_naturaleza),
    LENGUAJE("Lenguaje", R.drawable.ic_h_estudiar)
}

/** La opcion correcta es siempre la primera; la pantalla las baraja al pintar. */
data class Pregunta(val enunciado: String, val opciones: List<String>)

data class Capsula(
    val id: String,
    val categoria: Categoria,
    val gancho: String,
    val texto: String,
    val fuente: String,
    val preguntas: List<Pregunta>
)

val CAPSULAS: List<Capsula> = listOf(

    // ------------------------------------------------------ cuerpo humano ---

    Capsula(
        id = "huesos",
        categoria = Categoria.CUERPO,
        gancho = "¿Sabes cuántos huesos tiene el cuerpo humano?",
        texto = "Doscientos seis, en un adulto. Pero un recién nacido tiene bastantes " +
            "más, y no es que perdamos ninguno por el camino: es que muchos empiezan " +
            "separados y se sueldan al crecer. El cráneo de un bebé son varias placas " +
            "unidas por tejido blando —las fontanelas— para que la cabeza pueda " +
            "deformarse al pasar por el canal del parto y para dejar sitio a un cerebro " +
            "que todavía tiene que crecer mucho. El sacro, ese hueso plano y triangular " +
            "del final de la columna, son en realidad cinco vértebras que se fusionan " +
            "durante la adolescencia. Y el hueso más pequeño de todos es el estribo, en " +
            "el oído medio: mide unos tres milímetros.",
        fuente = "Anatomía humana, consenso de manuales",
        preguntas = listOf(
            Pregunta("¿Cuántos huesos tiene un adulto?",
                listOf("206", "180", "224", "195")),
            Pregunta("¿Cuántas vértebras se fusionan para formar el sacro?",
                listOf("Cinco", "Tres", "Siete", "Nueve")),
            Pregunta("¿Dónde está el estribo, el hueso más pequeño?",
                listOf("En el oído medio", "En la muñeca", "En la nariz", "En el pie"))
        )
    ),

    Capsula(
        id = "cosquillas",
        categoria = Categoria.CUERPO,
        gancho = "¿Sabes por qué no puedes hacerte cosquillas a ti mismo?",
        texto = "Porque tu cerebro te ve venir. El cerebelo predice las consecuencias " +
            "sensoriales de tus propios movimientos y resta esa predicción de lo que " +
            "acabas percibiendo; así distingue lo que te pasa de lo que te haces tú. " +
            "Un equipo del University College de Londres lo comprobó construyendo una " +
            "máquina de hacer cosquillas manejada por el propio sujeto. Cuando la " +
            "máquina respondía al instante, no había cosquillas. Al introducir un " +
            "retardo entre el movimiento de la mano y el roce, la sensación volvía, y " +
            "con doscientos milisegundos de desfase era casi tan intensa como si la " +
            "manejara otra persona. La predicción había dejado de cuadrar.",
        fuente = "Blakemore, Wolpert y Frith, University College London",
        preguntas = listOf(
            Pregunta("¿Qué parte del cerebro predice y cancela la sensación?",
                listOf("El cerebelo", "El hipocampo", "La amígdala", "El bulbo raquídeo")),
            Pregunta("¿Qué había que introducir para que volvieran las cosquillas?",
                listOf("Un retardo", "Más presión", "Un sonido", "Frío")),
            Pregunta("¿Cuántos milisegundos de desfase bastaban?",
                listOf("Doscientos", "Veinte", "Mil", "Cincuenta"))
        )
    ),

    Capsula(
        id = "corazon",
        categoria = Categoria.CUERPO,
        gancho = "¿Sabes cuánta sangre mueve tu corazón mientras estás sentado?",
        texto = "Unos cinco litros por minuto en reposo, más o menos todo el volumen de " +
            "sangre que tienes. Es decir: cada minuto, sin que hagas nada, tu sangre " +
            "entera da una vuelta completa. En un día son unos siete mil litros y cerca " +
            "de cien mil latidos. Durante un esfuerzo intenso el caudal puede " +
            "multiplicarse por cinco. Lo notable es que el músculo cardíaco no se " +
            "cansa como los demás: tiene muchísimas más mitocondrias por célula, no " +
            "acumula fatiga del mismo modo y descansa entre latido y latido, en esa " +
            "pausa que es más larga que la contracción.",
        fuente = "Fisiología cardiovascular, valores de referencia en reposo",
        preguntas = listOf(
            Pregunta("¿Cuántos litros por minuto bombea el corazón en reposo?",
                listOf("Unos cinco", "Unos veinte", "Medio litro", "Unos cincuenta")),
            Pregunta("¿Cuántos latidos da al día, aproximadamente?",
                listOf("Cien mil", "Diez mil", "Un millón", "Quinientos mil")),
            Pregunta("¿Qué tiene el músculo cardíaco en mayor cantidad que los demás?",
                listOf("Mitocondrias", "Grasa", "Colágeno", "Terminaciones nerviosas"))
        )
    ),

    // --------------------------------------------------------- astronomía ---

    Capsula(
        id = "luzsol",
        categoria = Categoria.ESPACIO,
        gancho = "¿Sabes cuánto tarda en llegarte la luz del Sol?",
        texto = "Ocho minutos y veinte segundos, contando desde que sale de la " +
            "superficie solar. Lo interesante es lo que pasó antes: esa energía se " +
            "generó en el núcleo por fusión nuclear y tuvo que atravesar capas " +
            "densísimas rebotando de partícula en partícula, en un recorrido caótico " +
            "que dura decenas de miles de años. La luz que te está calentando la cara " +
            "nació cuando en la Tierra todavía no existía la escritura. Otra cosa que " +
            "casi nadie sabe: el Sol es blanco. Lo vemos amarillo porque la atmósfera " +
            "dispersa la luz azul en todas direcciones —por eso el cielo es azul— y al " +
            "disco solar le quita ese azul, dejándolo amarillento.",
        fuente = "Astrofísica solar, NASA",
        preguntas = listOf(
            Pregunta("¿Cuánto tarda la luz del Sol en llegar a la Tierra?",
                listOf("Ocho minutos y veinte segundos", "Un segundo y medio",
                    "Unas dos horas", "Cuarenta segundos")),
            Pregunta("¿De qué color es realmente el Sol?",
                listOf("Blanco", "Amarillo", "Naranja", "Rojo pálido")),
            Pregunta("¿Por qué lo vemos amarillo desde aquí?",
                listOf("La atmósfera dispersa la luz azul", "Por la distancia",
                    "Por el polvo solar", "Por la temperatura de su superficie"))
        )
    ),

    Capsula(
        id = "voyager",
        categoria = Categoria.ESPACIO,
        gancho = "¿Sabes cuál de las dos Voyager se lanzó primero?",
        texto = "La Voyager 2, dieciséis días antes que la Voyager 1, en el verano de " +
            "1977. Los números no van por orden de lanzamiento sino por orden de " +
            "llegada a Júpiter: la Voyager 1 salió después pero tomó una trayectoria " +
            "más rápida y adelantó a su gemela por el camino. Las dos llevan atornillado " +
            "un disco de oro con sonidos e imágenes de la Tierra, música de varias " +
            "culturas y saludos en decenas de idiomas, pensado para quien pudiera " +
            "encontrarlo dentro de millones de años. La Voyager 1 es hoy el objeto " +
            "fabricado por humanos que más lejos ha llegado, y sigue enviando datos " +
            "desde fuera de la influencia del viento solar.",
        fuente = "NASA, Jet Propulsion Laboratory",
        preguntas = listOf(
            Pregunta("¿Cuál de las dos se lanzó primero?",
                listOf("La Voyager 2", "La Voyager 1", "Salieron el mismo día",
                    "No se sabe con certeza")),
            Pregunta("¿Por qué llevan esos números?",
                listOf("Por el orden de llegada a Júpiter", "Por orden de lanzamiento",
                    "Por el tamaño de cada sonda", "Por el país que las financió")),
            Pregunta("¿Qué llevan las dos a bordo?",
                listOf("Un disco de oro", "Una cápsula con semillas",
                    "Un reloj atómico de repuesto", "Una bandera de Naciones Unidas"))
        )
    ),

    Capsula(
        id = "nubes",
        categoria = Categoria.ESPACIO,
        gancho = "¿Sabes cuánto pesa una nube?",
        texto = "Un cúmulo de los normales, de esos blancos y algodonosos de un día " +
            "despejado, ocupa alrededor de un kilómetro cúbico y contiene medio gramo " +
            "de agua por metro cúbico. Multiplicando sale una cifra que cuesta creer: " +
            "unas quinientas toneladas de agua flotando sobre tu cabeza, el peso de " +
            "cien elefantes. No se cae por dos razones. La primera es que las gotas son " +
            "diminutas, de centésimas de milímetro, y a ese tamaño la resistencia del " +
            "aire domina sobre el peso, así que caen a una lentitud ridícula. La " +
            "segunda es que el aire que forma la nube va subiendo, y las sostiene.",
        fuente = "Meteorología, valores típicos de cúmulo",
        preguntas = listOf(
            Pregunta("¿Cuánto pesa aproximadamente un cúmulo normal?",
                listOf("Unas quinientas toneladas", "Unos cien kilos",
                    "Unas cinco toneladas", "Cerca de un millón de toneladas")),
            Pregunta("¿Qué volumen se toma como referencia?",
                listOf("Un kilómetro cúbico", "Un metro cúbico",
                    "Cien metros cúbicos", "Mil kilómetros cúbicos")),
            Pregunta("¿Por qué no se precipita al suelo?",
                listOf("Las gotas son diminutas y el aire asciende",
                    "El agua está en forma de gas", "La sostiene el campo magnético",
                    "Está más caliente que el aire de abajo"))
        )
    )
,

    // ----------------------------------------------------------- historia ---

    Capsula(
        id = "zanzibar",
        categoria = Categoria.HISTORIA,
        gancho = "¿Sabes cuánto duró la guerra más corta de la historia?",
        texto = "Treinta y ocho minutos, el 27 de agosto de 1896, entre el Reino Unido " +
            "y el sultanato de Zanzíbar. Había muerto el sultán, afín a los británicos, " +
            "y su sobrino Khalid se instaló en el palacio sin el visto bueno de Londres. " +
            "Los británicos dieron un ultimátum que expiraba a las nueve de la mañana. " +
            "A las nueve y dos minutos, los barcos anclados frente a la costa abrieron " +
            "fuego contra el palacio. A las nueve y cuarenta ondeaba la bandera de " +
            "rendición. Del lado zanzibarí hubo unas quinientas bajas; del británico, un " +
            "marinero herido. Khalid escapó por la parte de atrás y se refugió en el " +
            "consulado alemán, que se negó a entregarlo.",
        fuente = "Encyclopædia Britannica, «Anglo-Zanzibar War»",
        preguntas = listOf(
            Pregunta("¿Cuánto duró la guerra?",
                listOf("Treinta y ocho minutos", "Tres horas", "Dos días", "Once minutos")),
            Pregunta("¿A qué hora expiraba el ultimátum británico?",
                listOf("A las nueve de la mañana", "A medianoche",
                    "Al mediodía", "Al amanecer")),
            Pregunta("¿Dónde se refugió Khalid al huir?",
                listOf("En el consulado alemán", "En un barco pesquero",
                    "En la mezquita mayor", "En el consulado francés"))
        )
    ),

    Capsula(
        id = "tambora",
        categoria = Categoria.HISTORIA,
        gancho = "¿Sabes qué año se quedó sin verano?",
        texto = "1816. El año anterior había entrado en erupción el Tambora, en " +
            "Indonesia, con la explosión volcánica más violenta de la que hay registro. " +
            "La ceniza y los sulfatos que subieron a la estratosfera se repartieron por " +
            "el planeta y bloquearon parte de la luz del Sol durante meses. En Nueva " +
            "Inglaterra nevó en junio; en Europa se perdieron las cosechas y hubo " +
            "hambrunas y disturbios por el precio del pan. Aquel verano miserable, un " +
            "grupo de ingleses quedó encerrado por la lluvia en una casa junto al lago " +
            "de Ginebra, Villa Diodati. Byron propuso que cada uno escribiera una " +
            "historia de fantasmas para pasar el rato. De ahí salieron dos: Mary Shelley " +
            "empezó Frankenstein y John Polidori escribió El vampiro.",
        fuente = "Registro histórico de la erupción del Tambora (1815)",
        preguntas = listOf(
            Pregunta("¿Qué volcán provocó el año sin verano?",
                listOf("El Tambora", "El Krakatoa", "El Vesubio", "El Etna")),
            Pregunta("¿En qué casa se refugiaron del mal tiempo?",
                listOf("Villa Diodati", "Villa Borghese",
                    "La abadía de Newstead", "La casa de Rousseau")),
            Pregunta("¿Qué obra escribió Polidori aquel verano?",
                listOf("El vampiro", "Frankenstein", "Drácula", "El monje"))
        )
    ),

    Capsula(
        id = "petrov",
        categoria = Categoria.HISTORIA,
        gancho = "¿Sabes quién decidió no informar de un ataque nuclear?",
        texto = "Stanislav Petrov era el oficial de guardia en un búnker soviético la " +
            "madrugada del 26 de septiembre de 1983, cuando el sistema de alerta " +
            "temprana avisó del lanzamiento de un misil estadounidense. Segundos " +
            "después marcó cuatro más: cinco en total. El protocolo era informar a la " +
            "cadena de mando, y la cadena de mando habría tenido minutos para decidir " +
            "una respuesta. Petrov razonó que un primer ataque real no empezaría con " +
            "cinco misiles sino con centenares, y que el sistema llevaba poco tiempo " +
            "en funcionamiento. Reportó una avería. Tenía razón: los satélites habían " +
            "confundido el reflejo del sol en nubes de gran altitud con lanzamientos. " +
            "No lo condecoraron; su unidad quedó en evidencia y él acabó reasignado.",
        fuente = "Documentación desclasificada del incidente de 1983",
        preguntas = listOf(
            Pregunta("¿Cuántos misiles llegó a marcar el sistema en total?",
                listOf("Cinco", "Uno", "Doce", "Cuarenta")),
            Pregunta("¿Cuál fue el razonamiento de Petrov?",
                listOf("Un ataque real habría sido de centenares de misiles",
                    "Los radares no habían confirmado nada",
                    "Era demasiado pronto para un ataque",
                    "Había recibido aviso previo de Moscú")),
            Pregunta("¿Cuál era la causa real de la falsa alarma?",
                listOf("El reflejo del sol en nubes altas", "Un fallo eléctrico",
                    "Un misil de prueba soviético", "Una interferencia de radio"))
        )
    ),

    // --------------------------------------------------------------- arte ---

    Capsula(
        id = "estatuas",
        categoria = Categoria.ARTE,
        gancho = "¿Sabes de qué color eran las estatuas griegas?",
        texto = "De todos menos blanco. El mármol desnudo que asociamos a la Antigüedad " +
            "es un accidente: la pintura se fue con los siglos y nosotros confundimos la " +
            "ruina con el diseño. Las estatuas iban cubiertas de rojo, azul, ocre y " +
            "dorado, con los ojos pintados y las telas estampadas con dibujos. El " +
            "arqueólogo Vinzenz Brinkmann lo demostró iluminando las piezas con luz " +
            "rasante y con ultravioleta: la primera revela el relieve microscópico que " +
            "dejó la pintura al desprenderse, y la segunda hace brillar restos de " +
            "pigmento invisibles a simple vista. El ideal del blanco puro lo consagró " +
            "Winckelmann en el siglo XVIII, y de ahí pasó al neoclasicismo, a los museos " +
            "y a nuestra cabeza.",
        fuente = "Vinzenz Brinkmann, proyecto «Bunte Götter» (Dioses en color)",
        preguntas = listOf(
            Pregunta("¿Qué dos tipos de luz usó Brinkmann?",
                listOf("Rasante y ultravioleta", "Infrarroja y láser",
                    "Polarizada y de sodio", "Estroboscópica y azul")),
            Pregunta("¿Qué revela la luz rasante?",
                listOf("El relieve que dejó la pintura al desprenderse",
                    "Los restos de pigmento invisibles", "Las grietas internas",
                    "La firma del escultor")),
            Pregunta("¿Quién consagró el ideal del mármol blanco?",
                listOf("Winckelmann", "Brinkmann", "Fidias", "Canova"))
        )
    ),

    Capsula(
        id = "ultramar",
        categoria = Categoria.ARTE,
        gancho = "¿Sabes cuál fue el color más caro de la historia?",
        texto = "El azul ultramar, que se obtenía moliendo lapislázuli. La piedra venía " +
            "de unas minas de Afganistán y había que traerla a Europa por rutas " +
            "larguísimas; de ahí el nombre, «ultramarinus», el que viene de más allá del " +
            "mar. El proceso de extraer el pigmento puro era además lento y " +
            "desperdiciaba casi todo el material, así que el resultado llegó a costar " +
            "más que el oro a igual peso. Los contratos que firmaban los pintores del " +
            "Renacimiento especificaban por escrito cuánto ultramar debía llevar el " +
            "cuadro, igual que hoy se especifica un material en una obra. Por eso el " +
            "manto de la Virgen suele ser azul: no era solo simbolismo, era gasto. En " +
            "1826 se consiguió sintetizarlo en laboratorio y el precio se desplomó.",
        fuente = "Historia de los pigmentos; síntesis del ultramar artificial (1826)",
        preguntas = listOf(
            Pregunta("¿De qué piedra se obtenía el ultramar?",
                listOf("Lapislázuli", "Malaquita", "Turquesa", "Azurita")),
            Pregunta("¿De dónde venía la piedra?",
                listOf("De Afganistán", "De Egipto", "De Perú", "De la India")),
            Pregunta("¿Qué especificaban por escrito los contratos de los pintores?",
                listOf("Cuánto ultramar debía llevar el cuadro",
                    "El plazo de entrega exacto", "El número de figuras",
                    "El tamaño del bastidor"))
        )
    ),

    Capsula(
        id = "maravillas",
        categoria = Categoria.ARTE,
        gancho = "De las siete maravillas del mundo antiguo, ¿cuál sigue en pie?",
        texto = "Solo una, la Gran Pirámide de Guiza, que además es con diferencia la " +
            "más antigua de la lista: cuando se construyeron las otras seis, ella " +
            "llevaba ya dos mil años levantada. Las demás cayeron por terremotos, " +
            "incendios o abandono. El Coloso de Rodas, la estatua de bronce que " +
            "guardaba el puerto, medía unos treinta y tres metros y un terremoto lo " +
            "derribó; lo llamativo es lo que pasó después: nadie lo reconstruyó y sus " +
            "restos siguieron tirados en el suelo unos novecientos años, hasta que en " +
            "el siglo VII se vendió el bronce. La lista tampoco es sagrada: procede de " +
            "guías de viaje griegas, y de los Jardines Colgantes de Babilonia no existe " +
            "una sola prueba arqueológica.",
        fuente = "Fuentes clásicas sobre el Coloso; Wikipedia en español",
        preguntas = listOf(
            Pregunta("¿Qué altura tenía el Coloso de Rodas?",
                listOf("Unos treinta y tres metros", "Unos diez metros",
                    "Unos noventa metros", "Unos sesenta metros")),
            Pregunta("¿Cuánto tiempo permanecieron sus restos en el suelo?",
                listOf("Unos novecientos años", "Unos veinte años",
                    "Unos doscientos años", "Menos de un siglo")),
            Pregunta("¿De qué maravilla no hay ninguna prueba arqueológica?",
                listOf("De los Jardines Colgantes de Babilonia",
                    "Del Faro de Alejandría", "Del Templo de Artemisa",
                    "Del Mausoleo de Halicarnaso"))
        )
    )
,

    // ------------------------------------------------------------ animales ---

    Capsula(
        id = "pulpo",
        categoria = Categoria.ANIMALES,
        gancho = "¿Sabes cuántos corazones tiene un pulpo?",
        texto = "Tres. Dos bombean sangre a las branquias y el tercero la reparte por el " +
            "resto del cuerpo; ese tercero se para cuando el pulpo nada, lo que explica " +
            "que prefiera caminar por el fondo antes que nadar, porque nadar lo agota. " +
            "Su sangre no es roja sino azulada, porque en lugar de hemoglobina con " +
            "hierro usa hemocianina, que lleva cobre y transporta el oxígeno mejor en " +
            "agua fría. Y lo más raro está en el sistema nervioso: alrededor de dos " +
            "tercios de sus neuronas no están en el cerebro sino repartidas por los " +
            "brazos, que pueden explorar y resolver problemas con bastante autonomía. " +
            "Cambian de color para camuflarse, y sin embargo casi todas las especies " +
            "conocidas son daltónicas.",
        fuente = "Biología de los cefalópodos",
        preguntas = listOf(
            Pregunta("¿Cuántos corazones tiene un pulpo?",
                listOf("Tres", "Uno", "Dos", "Cinco")),
            Pregunta("¿Qué metal transporta el oxígeno en su sangre?",
                listOf("Cobre", "Hierro", "Zinc", "Magnesio")),
            Pregunta("¿Dónde está la mayoría de sus neuronas?",
                listOf("En los brazos", "En el cerebro", "Alrededor de los ojos",
                    "En el manto"))
        )
    ),

    Capsula(
        id = "tardigrados",
        categoria = Categoria.ANIMALES,
        gancho = "¿Sabes qué animal sobrevivió al vacío del espacio?",
        texto = "El tardígrado, un bicho de menos de un milímetro que vive en el musgo " +
            "húmedo y camina despacio con ocho patas rechonchas. Cuando las condiciones " +
            "se ponen imposibles se deshidrata casi por completo, se encoge en una " +
            "bolita y entra en un estado llamado criptobiosis, con el metabolismo " +
            "prácticamente detenido; puede pasar años así y revivir al mojarlo. En ese " +
            "estado aguanta temperaturas extremas, presiones enormes y radiación. En " +
            "2007 se enviaron varios a la órbita terrestre y se los expuso directamente " +
            "al vacío del espacio; algunos volvieron vivos y llegaron a reproducirse. " +
            "El nombre se lo puso Spallanzani y significa «el que anda despacio»; antes " +
            "Goeze los había llamado ositos de agua.",
        fuente = "Jönsson et al., Current Biology (2008), experimento en órbita",
        preguntas = listOf(
            Pregunta("¿Cómo se llama el estado en que se deshidratan?",
                listOf("Criptobiosis", "Hibernación", "Diapausa", "Estivación")),
            Pregunta("¿Qué significa la palabra «tardígrado»?",
                listOf("El que anda despacio", "Oso de agua",
                    "El que resiste", "De ocho patas")),
            Pregunta("¿A qué se los expuso en el experimento de 2007?",
                listOf("Al vacío del espacio", "A un reactor nuclear",
                    "Al fondo de una fosa marina", "A nitrógeno líquido"))
        )
    ),

    Capsula(
        id = "abejas",
        categoria = Categoria.ANIMALES,
        gancho = "¿Sabes cómo le explica una abeja a otra dónde están las flores?",
        texto = "Bailando. La abeja exploradora vuelve al panal y traza sobre la " +
            "superficie vertical un recorrido en forma de ocho, con un tramo recto en " +
            "medio que hace vibrando el abdomen. Ese tramo lo dice todo. El ángulo que " +
            "forma con la vertical del panal es el mismo ángulo que hay que tomar " +
            "respecto al sol al salir volando, y la duración del meneo indica lo lejos " +
            "que está la comida: cuanto más largo, más lejos. Como el sol se mueve, la " +
            "abeja corrige el ángulo según pasan las horas. Lo descifró Karl von " +
            "Frisch tras décadas observando colmenas, y le valió el premio Nobel de " +
            "Medicina en 1973, compartido con Lorenz y Tinbergen.",
        fuente = "Premio Nobel de Fisiología o Medicina, 1973",
        preguntas = listOf(
            Pregunta("¿Qué forma tiene el recorrido de la danza?",
                listOf("Un ocho", "Un círculo", "Un triángulo", "Una espiral")),
            Pregunta("¿Qué indica la duración del meneo?",
                listOf("La distancia a la comida", "La cantidad de néctar",
                    "La dirección del viento", "El número de flores")),
            Pregunta("¿En qué año recibió von Frisch el Nobel?",
                listOf("1973", "1953", "1988", "1962"))
        )
    ),

    // ----------------------------------------------------------- lenguaje ---

    Capsula(
        id = "alfabeto",
        categoria = Categoria.LENGUAJE,
        gancho = "¿Sabes qué significaban las primeras letras del alfabeto?",
        texto = "Buey y casa. Las letras nacieron como dibujos de cosas: la primera del " +
            "alfabeto semítico era «alef», que significaba buey, y si le das la vuelta a " +
            "nuestra A todavía se reconoce una cabeza con dos cuernos. La segunda era " +
            "«bet», casa, de donde viene también la palabra Betlehem, casa del pan. Los " +
            "griegos tomaron prestado el sistema entero pero le cambiaron una cosa " +
            "decisiva: la alef, que en las lenguas semíticas representaba una consonante " +
            "que ellos no usaban, la reaprovecharon como vocal y la llamaron alfa. Ese " +
            "invento —escribir también las vocales— es lo que convirtió el alfabeto en " +
            "algo que se aprende en meses. Y «alfabeto» es literalmente alfa más beta, " +
            "es decir buey más casa.",
        fuente = "Historia de la escritura; origen del alfabeto semítico",
        preguntas = listOf(
            Pregunta("¿Qué significaba «alef»?",
                listOf("Buey", "Casa", "Agua", "Mano")),
            Pregunta("¿Qué significaba «bet»?",
                listOf("Casa", "Buey", "Puerta", "Pez")),
            Pregunta("¿Qué hicieron los griegos con la alef?",
                listOf("La convirtieron en una vocal", "La eliminaron del alfabeto",
                    "Le cambiaron el dibujo", "La pusieron al final"))
        )
    ),

    Capsula(
        id = "linealb",
        categoria = Categoria.LENGUAJE,
        gancho = "¿Sabes quién descifró una escritura que llevaba medio siglo resistiéndose?",
        texto = "Un arquitecto aficionado. Las tablillas de arcilla con signos " +
            "desconocidos aparecieron en Creta a principios del siglo XX, y durante " +
            "cincuenta años ningún especialista logró leerlas; se las llamó lineal B. " +
            "Michael Ventris se había obsesionado con ellas de niño, y siguió " +
            "trabajando por su cuenta mientras ejercía de arquitecto. En 1952 anunció " +
            "la solución, y resultó ser lo que casi nadie esperaba: la lengua escrita " +
            "en aquellas tablillas era griego, unos quinientos años anterior a Homero. " +
            "El propio Evans, que había excavado las tablillas, había insistido toda su " +
            "vida en que no podía serlo. Ventris murió en un accidente de tráfico cuatro " +
            "años después del descubrimiento, a los treinta y cuatro años.",
        fuente = "Desciframiento del lineal B por Michael Ventris (1952)",
        preguntas = listOf(
            Pregunta("¿Qué lengua resultó estar escrita en lineal B?",
                listOf("Griego", "Fenicio", "Hitita", "Una lengua desconocida")),
            Pregunta("¿A qué se dedicaba Ventris?",
                listOf("Era arquitecto", "Era catedrático de griego",
                    "Era criptógrafo militar", "Era arqueólogo")),
            Pregunta("¿En qué año anunció la solución?",
                listOf("1952", "1922", "1936", "1961"))
        )
    ),

    Capsula(
        id = "rosetta",
        categoria = Categoria.LENGUAJE,
        gancho = "¿Sabes cómo se consiguió leer los jeroglíficos egipcios?",
        texto = "Gracias a una piedra encontrada por casualidad en 1799 por soldados de " +
            "la expedición de Napoleón, mientras levantaban una fortificación cerca de " +
            "la ciudad de Rashid, que los europeos llamaban Rosetta. La losa llevaba " +
            "grabado el mismo decreto en tres versiones: jeroglíficos, escritura " +
            "demótica y griego. Como el griego sí se sabía leer, había por fin un punto " +
            "de comparación. Aun así costó más de veinte años. La clave la encontró " +
            "Champollion al comprender que los jeroglíficos no eran solo símbolos de " +
            "ideas, como se creía, sino que muchos representaban sonidos; los nombres " +
            "propios encerrados en óvalos —los cartuchos— le dieron el punto de apoyo, y " +
            "en 1822 hizo público el desciframiento.",
        fuente = "Desciframiento de los jeroglíficos por Champollion (1822)",
        preguntas = listOf(
            Pregunta("¿En qué año se encontró la piedra?",
                listOf("1799", "1822", "1750", "1830")),
            Pregunta("¿Cuántas versiones del mismo texto lleva grabadas?",
                listOf("Tres", "Dos", "Cuatro", "Una sola")),
            Pregunta("¿Qué son los cartuchos?",
                listOf("Óvalos que encierran nombres propios",
                    "Los sellos de los escribas", "Las marcas de la cantera",
                    "Los signos que indican números"))
        )
    )
)

object Reto {

    /** Cuantas preguntas seguidas hay que acertar. Fallar una empieza de cero. */
    const val PREGUNTAS_POR_CAPSULA = 3

    /** Las categorias que se le ofrecen al usuario para elegir. */
    fun categoriasAlAzar(cuantas: Int = 3): List<Categoria> =
        Categoria.entries.shuffled().take(cuantas)

    /**
     * Una capsula de esa categoria que el usuario no haya leido todavia.
     *
     * Sin llevar la cuenta de lo visto, tarde o temprano te toca dos veces la
     * misma y la segunda ya no hay que leer nada. Cuando se agotan las de una
     * categoria se vuelve a empezar con ella, pero nunca repitiendo la de justo
     * antes.
     */
    fun siguiente(categoria: Categoria, vistas: Set<String>, evitar: String?): Capsula {
        val todas = CAPSULAS.filter { it.categoria == categoria }
        val frescas = todas.filter { it.id !in vistas && it.id != evitar }
        val pozo = frescas.ifEmpty { todas.filter { it.id != evitar }.ifEmpty { todas } }
        return pozo[Random.nextInt(pozo.size)]
    }

    /** Cuantas capsulas quedan por leer en una categoria, para enseñarlo al elegir. */
    fun frescasEn(categoria: Categoria, vistas: Set<String>): Int =
        CAPSULAS.count { it.categoria == categoria && it.id !in vistas }

    /**
     * Segundos que el boton de continuar permanece bloqueado.
     *
     * Calculado sobre las palabras del texto a un ritmo de lectura tranquilo.
     * Sin este tope bastaria con dar a continuar y jugar a adivinar: un reto que
     * se supera con suerte no es un reto, es un dado.
     */
    fun segundosDeLectura(capsula: Capsula): Int {
        val palabras = capsula.texto.split(' ').count { it.isNotBlank() }
        return (palabras / 3).coerceIn(25, 55)
    }
}
