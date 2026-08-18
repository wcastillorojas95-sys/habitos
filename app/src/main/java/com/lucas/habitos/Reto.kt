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
 * El enum se llama TemaReto y no Categoria porque ese nombre ya lo ocupa la
 * data class de las categorias de Explorar, en este mismo paquete.
 *
 * Sobre los datos: cada capsula lleva su fuente y las cifras que son respuesta
 * estan verificadas. Donde las fuentes discrepan, el dato no se usa como
 * pregunta. Una app que te hace leer para salir no puede enseñarte algo falso.
 */

enum class TemaReto(val etiqueta: String, val icono: Int) {
    CUERPO("Cuerpo humano", R.drawable.ic_h_corazon),
    ESPACIO("Astronomía", R.drawable.ic_h_sol),
    HISTORIA("Historia", R.drawable.ic_h_leer),
    ARTE("Arte", R.drawable.ic_h_escribir),
    ANIMALES("Animales", R.drawable.ic_h_naturaleza),
    LENGUAJE("Lenguaje", R.drawable.ic_h_estudiar),
    FILOSOFIA("Filosofía", R.drawable.ic_h_mente),
    PSICOLOGIA("Psicología", R.drawable.ic_h_meditar)
}

/** La opcion correcta es siempre la primera; la pantalla las baraja al pintar. */
data class Pregunta(val enunciado: String, val opciones: List<String>)

data class Capsula(
    val id: String,
    val categoria: TemaReto,
    val gancho: String,
    val texto: String,
    val fuente: String,
    val preguntas: List<Pregunta>
)

val CAPSULAS: List<Capsula> = listOf(

    // ------------------------------------------------------ cuerpo humano ---

    Capsula(
        id = "huesos",
        categoria = TemaReto.CUERPO,
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
        categoria = TemaReto.CUERPO,
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
        categoria = TemaReto.CUERPO,
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
        categoria = TemaReto.ESPACIO,
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
        categoria = TemaReto.ESPACIO,
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
        categoria = TemaReto.ESPACIO,
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
        categoria = TemaReto.HISTORIA,
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
        categoria = TemaReto.HISTORIA,
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
        categoria = TemaReto.HISTORIA,
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
        categoria = TemaReto.ARTE,
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
        categoria = TemaReto.ARTE,
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
        categoria = TemaReto.ARTE,
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
        categoria = TemaReto.ANIMALES,
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
        categoria = TemaReto.ANIMALES,
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
        categoria = TemaReto.ANIMALES,
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
        categoria = TemaReto.LENGUAJE,
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
        categoria = TemaReto.LENGUAJE,
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
        categoria = TemaReto.LENGUAJE,
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
,

    // --------------------------------------------------------- filosofía ---

    Capsula(
        id = "teseo",
        categoria = TemaReto.FILOSOFIA,
        gancho = "Si cambias todas las piezas de algo, ¿sigue siendo lo mismo?",
        texto = "Los atenienses conservaron durante siglos el barco en el que Teseo " +
            "había vuelto de Creta. Cada vez que una tabla se pudría la sustituían por " +
            "otra nueva, hasta que llegó un día en que no quedaba ni una sola pieza " +
            "original. Plutarco cuenta que los filósofos discutían si aquel seguía " +
            "siendo el barco de Teseo o era ya otro distinto. Siglos después Hobbes " +
            "añadió una vuelta de tuerca incómoda: imagina que alguien hubiera ido " +
            "guardando las tablas viejas y con ellas montara un segundo barco. " +
            "Entonces habría dos, y los dos con derecho a llamarse el original. La " +
            "pregunta no es sobre barcos: es sobre qué te hace seguir siendo tú.",
        fuente = "Plutarco, «Vida de Teseo»; el añadido es de Thomas Hobbes",
        preguntas = listOf(
            Pregunta("¿De dónde volvía Teseo en ese barco?",
                listOf("De Creta", "De Troya", "De Egipto", "De Esparta")),
            Pregunta("¿Qué vuelta de tuerca añadió Hobbes?",
                listOf("Montar un segundo barco con las tablas viejas",
                    "Quemar el barco original", "Contar cuántas tablas se cambiaron",
                    "Preguntar quién era el dueño")),
            Pregunta("¿Quién cuenta la historia del barco?",
                listOf("Plutarco", "Platón", "Homero", "Aristóteles"))
        )
    ),

    Capsula(
        id = "caverna",
        categoria = TemaReto.FILOSOFIA,
        gancho = "¿Y si lo que ves fueran solo sombras?",
        texto = "Platón pide que te imagines a unos prisioneros encadenados desde la " +
            "infancia en el fondo de una cueva, de cara a la pared y sin poder girar " +
            "la cabeza. Detrás de ellos arde un fuego, y entre el fuego y sus espaldas " +
            "pasan personas llevando objetos, cuyas sombras se proyectan en la pared " +
            "que miran. Para esos prisioneros, las sombras no son una representación " +
            "de las cosas: son las cosas. Si uno lograra soltarse y salir a la luz del " +
            "sol, al principio quedaría deslumbrado y no vería nada. Y si volviera a " +
            "contarlo, dice Platón, los demás pensarían que se ha vuelto loco, y " +
            "podrían incluso matarlo por insistir.",
        fuente = "Platón, «República», libro VII",
        preguntas = listOf(
            Pregunta("¿Qué hay detrás de los prisioneros?",
                listOf("Un fuego", "Una ventana", "Otra cueva", "Un espejo")),
            Pregunta("¿Qué le pasa al que sale, nada más salir?",
                listOf("Queda deslumbrado y no ve nada", "Se orienta enseguida",
                    "Pierde la memoria", "Encuentra a los demás fuera")),
            Pregunta("Según Platón, ¿cómo reaccionan los que se quedan dentro?",
                listOf("Creen que se ha vuelto loco", "Le siguen al momento",
                    "Le nombran su guía", "No se dan cuenta de que faltaba"))
        )
    ),

    Capsula(
        id = "genio",
        categoria = TemaReto.FILOSOFIA,
        gancho = "¿De qué puedes estar absolutamente seguro?",
        texto = "Descartes decidió dudar de todo lo que pudiera ser dudado, a ver qué " +
            "quedaba en pie. Los sentidos engañan, así que fuera. Los sueños parecen " +
            "reales mientras duran, así que ni siquiera estar despierto es seguro. Y " +
            "para llevarlo al extremo se inventó un genio maligno, un ser poderosísimo " +
            "dedicado a engañarle en todo momento: quizá el mundo entero, su cuerpo y " +
            "hasta las matemáticas fueran una ilusión montada por él. Pero ahí " +
            "encontró el suelo. Para ser engañado hay que existir. Aunque todo lo " +
            "demás sea falso, mientras pienso, existo. De ahí salió la frase más " +
            "citada de la filosofía moderna.",
        fuente = "Descartes, «Meditaciones metafísicas» (1641)",
        preguntas = listOf(
            Pregunta("¿Qué se inventó Descartes para dudar de todo?",
                listOf("Un genio maligno que le engaña", "Una máquina de sueños",
                    "Un espejo deformante", "Una ciudad imaginaria")),
            Pregunta("¿Por qué el engaño no puede alcanzarlo todo?",
                listOf("Para ser engañado hay que existir",
                    "Porque las matemáticas son ciertas",
                    "Porque Dios no lo permitiría",
                    "Porque los sentidos a veces aciertan")),
            Pregunta("¿De qué duda también, además de los sentidos?",
                listOf("De estar despierto", "De su nombre",
                    "De la existencia de otros filósofos", "Del idioma que habla"))
        )
    ),

    Capsula(
        id = "velo",
        categoria = TemaReto.FILOSOFIA,
        gancho = "¿Cómo repartirías el mundo si no supieras qué te va a tocar?",
        texto = "John Rawls propuso un truco para pensar la justicia sin hacer trampa. " +
            "Imagina que tienes que diseñar las reglas de una sociedad —los impuestos, " +
            "la sanidad, la educación, los derechos— pero desde detrás de lo que llamó " +
            "un velo de ignorancia: no sabes en qué familia vas a nacer, ni si serás " +
            "rico o pobre, hombre o mujer, sano o enfermo, listo o torpe. Lo decides " +
            "todo antes de saber quién serás. Rawls sostenía que en esa situación " +
            "nadie apostaría por una sociedad con miseria en el fondo, porque el fondo " +
            "podría tocarte a ti. La desigualdad solo se aceptaría si mejora la " +
            "situación de los que peor están.",
        fuente = "John Rawls, «Teoría de la justicia» (1971)",
        preguntas = listOf(
            Pregunta("¿Qué es lo que no sabes detrás del velo?",
                listOf("Quién vas a ser en esa sociedad", "Cuánto va a durar",
                    "Qué idioma se hablará", "Dónde está situada")),
            Pregunta("¿Cuándo se acepta la desigualdad, según Rawls?",
                listOf("Si mejora la situación de los que peor están",
                    "Si la vota la mayoría", "Si es pequeña", "Nunca, en ningún caso")),
            Pregunta("¿Qué hay que diseñar en el experimento?",
                listOf("Las reglas de una sociedad", "Una constitución para un solo día",
                    "El reparto de una herencia", "Una ciudad ideal"))
        )
    ),

    Capsula(
        id = "chino",
        categoria = TemaReto.FILOSOFIA,
        gancho = "¿Una máquina que responde bien entiende lo que dice?",
        texto = "John Searle pide que te imagines encerrado en una habitación. Por una " +
            "ranura te entran papeles con símbolos chinos, que tú no sabes leer. " +
            "Tienes un manual enorme, en tu idioma, que dice qué símbolos hay que " +
            "devolver ante cada combinación que entra. Sigues las reglas al pie de la " +
            "letra y sacas los papeles por la otra ranura. Fuera, un chino nativo lee " +
            "tus respuestas y las encuentra perfectas: está convencido de que dentro " +
            "hay alguien que domina el idioma. Pero tú no has entendido ni una palabra " +
            "en todo el rato. Searle lo usó contra la idea de que un ordenador que " +
            "manipula símbolos correctamente esté comprendiendo algo.",
        fuente = "John Searle, «Minds, Brains, and Programs» (1980)",
        preguntas = listOf(
            Pregunta("¿En qué idioma está el manual de reglas?",
                listOf("En el idioma de quien está dentro", "En chino",
                    "En latín", "No está escrito en ningún idioma")),
            Pregunta("¿Qué opina de las respuestas el chino que está fuera?",
                listOf("Que son perfectas", "Que tienen faltas leves",
                    "Que son incomprensibles", "Que las escribió una máquina")),
            Pregunta("¿Contra qué idea usó Searle el experimento?",
                listOf("Que manipular símbolos sea comprender",
                    "Que las máquinas puedan calcular", "Que el chino sea difícil",
                    "Que existan las traducciones automáticas"))
        )
    ),

    Capsula(
        id = "tranvia",
        categoria = TemaReto.FILOSOFIA,
        gancho = "¿Matarías a uno para salvar a cinco?",
        texto = "Philippa Foot planteó el caso en 1967 y no ha dejado de dar guerra. Un " +
            "tranvía sin frenos va directo hacia cinco personas atadas a la vía. Tú " +
            "estás junto a una palanca que lo desviaría a un ramal donde hay una sola " +
            "persona. ¿La accionas? La mayoría dice que sí. Judith Jarvis Thomson " +
            "añadió después una variante: ahora estás en un puente sobre la vía, junto " +
            "a un hombre muy corpulento, y la única forma de parar el tranvía es " +
            "empujarlo al vacío. Los números son idénticos, uno por cinco, pero casi " +
            "todo el mundo cambia de respuesta. Ese desajuste entre las dos versiones " +
            "es justamente lo interesante del problema.",
        fuente = "Philippa Foot (1967) y Judith Jarvis Thomson (1976)",
        preguntas = listOf(
            Pregunta("¿Quién planteó la versión original en 1967?",
                listOf("Philippa Foot", "Judith Jarvis Thomson",
                    "Peter Singer", "Bernard Williams")),
            Pregunta("¿Qué hay que hacer en la variante del puente?",
                listOf("Empujar a un hombre corpulento", "Accionar otra palanca",
                    "Avisar por megafonía", "Saltar tú mismo")),
            Pregunta("¿Qué pasa cuando se compara con la primera versión?",
                listOf("Casi todo el mundo cambia de respuesta",
                    "Las respuestas coinciden", "Nadie sabe qué contestar",
                    "Todos se niegan a responder"))
        )
    ),

    Capsula(
        id = "occam",
        categoria = TemaReto.FILOSOFIA,
        gancho = "Cuando hay varias explicaciones, ¿cuál eliges?",
        texto = "Guillermo de Ockham, fraile franciscano del siglo XIV, dejó escrito un " +
            "principio que suele resumirse así: no multipliques los entes sin " +
            "necesidad. Si dos explicaciones dan cuenta igual de bien de lo mismo, " +
            "quédate con la que menos cosas dé por supuestas. Ojo con el matiz, que se " +
            "pierde siempre: no dice que la explicación simple sea la verdadera, ni " +
            "que la realidad tenga que ser sencilla. Dice que, a igualdad de poder " +
            "explicativo, añadir suposiciones extra no te aporta nada y sí te da más " +
            "sitios donde equivocarte. Es una regla para elegir por dónde empezar, no " +
            "un veredicto sobre cómo es el mundo.",
        fuente = "Guillermo de Ockham (siglo XIV); la formulación es posterior",
        preguntas = listOf(
            Pregunta("¿A qué orden religiosa pertenecía Ockham?",
                listOf("Franciscana", "Dominica", "Benedictina", "Jesuita")),
            Pregunta("¿Qué dice exactamente el principio?",
                listOf("No multiplicar los entes sin necesidad",
                    "Que lo simple siempre es verdadero",
                    "Que hay que dudar de todo",
                    "Que la naturaleza no da saltos")),
            Pregunta("¿Qué NO afirma la navaja, según el texto?",
                listOf("Que la explicación simple sea la verdadera",
                    "Que sirva para elegir por dónde empezar",
                    "Que las suposiciones extra añadan riesgo",
                    "Que se apliquen a explicaciones igual de buenas"))
        )
    ),

    Capsula(
        id = "retorno",
        categoria = TemaReto.FILOSOFIA,
        gancho = "¿Repetirías tu vida exactamente igual, infinitas veces?",
        texto = "Nietzsche lo plantea como una visita nocturna. Imagina que un demonio " +
            "se cuela en tu momento más solitario y te dice que esta vida, tal como la " +
            "has vivido y la estás viviendo, tendrás que vivirla otra vez, y otra, " +
            "infinitas veces; y que no habrá nada nuevo en ella, sino cada dolor y " +
            "cada alegría y cada pensamiento en la misma sucesión exacta, hasta la " +
            "araña y la luz de la luna entre los árboles. La pregunta de Nietzsche no " +
            "es si eso es verdad. Es qué harías al oírlo: si te dejarías caer " +
            "rechinando los dientes y maldiciendo, o si contestarías que nunca oíste " +
            "nada más divino.",
        fuente = "Nietzsche, «La gaya ciencia», parágrafo 341 (1882)",
        preguntas = listOf(
            Pregunta("¿Quién trae la noticia en el relato de Nietzsche?",
                listOf("Un demonio", "Un ángel", "Un viejo maestro", "Nadie, es un sueño")),
            Pregunta("¿Qué dos detalles menciona el texto que se repetirían?",
                listOf("La araña y la luz de la luna entre los árboles",
                    "El vino y el pan de la cena", "El reloj y la ventana",
                    "El viento y el mar")),
            Pregunta("¿Cuál es la verdadera pregunta de Nietzsche?",
                listOf("Qué harías al oírlo", "Si el eterno retorno es cierto",
                    "Cuántas veces se repetiría", "Si el demonio existe"))
        )
    )
,

    // -------------------------------------------------------- psicología ---

    Capsula(
        id = "zeigarnik",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Por qué te acuerdas mejor de lo que dejaste a medias?",
        texto = "Bluma Zeigarnik se fijó en algo mientras observaba a los camareros de " +
            "un café: recordaban con precisión los pedidos que aún no habían cobrado, " +
            "y en cuanto cobraban se les borraban de la cabeza. Lo llevó al " +
            "laboratorio, encargando a los participantes tareas sencillas e " +
            "interrumpiendo la mitad de ellas antes de terminar. Al preguntarles " +
            "después qué recordaban, las tareas interrumpidas se recordaban bastante " +
            "mejor que las acabadas. La explicación que dio es que una tarea abierta " +
            "mantiene una tensión activa que no se libera hasta cerrarla. Es la razón " +
            "de que las series terminen los capítulos a mitad de escena.",
        fuente = "Bluma Zeigarnik (1927), laboratorio de Kurt Lewin en Berlín",
        preguntas = listOf(
            Pregunta("¿En qué oficio se fijó primero Zeigarnik?",
                listOf("Camareros de un café", "Carteros", "Telefonistas", "Actores")),
            Pregunta("¿Qué hizo en el laboratorio con la mitad de las tareas?",
                listOf("Interrumpirlas antes de terminar", "Repetirlas dos veces",
                    "Hacerlas más difíciles", "Encargarlas por escrito")),
            Pregunta("¿Qué se recordaba mejor?",
                listOf("Las tareas interrumpidas", "Las tareas acabadas",
                    "Las dos por igual", "Solo las primeras de la lista"))
        )
    ),

    Capsula(
        id = "malvavisco",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Esperarías quince minutos por una golosina más?",
        texto = "Walter Mischel sentaba a un niño ante una golosina y le daba una " +
            "opción: cómetela ahora, o espera a que yo vuelva y te daré dos. Luego " +
            "salía de la habitación. Algunos aguantaban tapándose los ojos, cantando o " +
            "dándose la vuelta; otros duraban segundos. Años después, los que habían " +
            "esperado tenían de media mejores resultados académicos, y aquello se " +
            "convirtió en la prueba estrella de la fuerza de voluntad. Pero la " +
            "historia tiene una segunda parte menos citada: al repetirlo en 2018 con " +
            "una muestra mucho mayor y más variada, el efecto se encogió bastante, y " +
            "buena parte de lo que predecía la espera se explicaba por el entorno " +
            "familiar del niño.",
        fuente = "Walter Mischel, Stanford (1972); replicación de Watts y otros (2018)",
        preguntas = listOf(
            Pregunta("¿Qué le ofrecían al niño si esperaba?",
                listOf("Dos golosinas en vez de una", "Un juguete",
                    "Salir antes al recreo", "Nada, solo se le pedía")),
            Pregunta("¿Qué pasó al repetir el estudio en 2018?",
                listOf("El efecto se encogió bastante", "Se confirmó igual de fuerte",
                    "No se pudo repetir", "Salió el resultado contrario")),
            Pregunta("¿Qué explicaba buena parte de la diferencia en la replicación?",
                listOf("El entorno familiar del niño", "La hora del día",
                    "El sabor de la golosina", "La edad exacta"))
        )
    ),

    Capsula(
        id = "dunning",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Por qué el que menos sabe es el que más seguro está?",
        texto = "Todo empezó con un atracador de bancos. Un hombre asaltó dos bancos a " +
            "cara descubierta convencido de que el zumo de limón que se había untado " +
            "en la cara lo haría invisible a las cámaras, y se quedó atónito al ser " +
            "detenido. El caso le llamó la atención a David Dunning, que junto a " +
            "Justin Kruger montó una serie de pruebas de lógica, gramática y humor en " +
            "Cornell. Después de cada prueba pedían a los participantes que estimaran " +
            "su propio resultado. Los que peor puntuaban eran los que más se " +
            "sobrestimaban, y por un margen enorme. La razón que propusieron es " +
            "incómoda: para saber lo mal que lo estás haciendo hace falta justo la " +
            "competencia que te falta.",
        fuente = "Kruger y Dunning, Universidad de Cornell (1999)",
        preguntas = listOf(
            Pregunta("¿Qué creía el atracador que lo hacía invisible?",
                listOf("Zumo de limón en la cara", "Una máscara transparente",
                    "Un imán en el bolsillo", "Salir a mediodía")),
            Pregunta("¿En qué universidad se hicieron las pruebas?",
                listOf("Cornell", "Harvard", "Stanford", "Yale")),
            Pregunta("¿Qué se les pedía tras cada prueba?",
                listOf("Estimar su propio resultado", "Corregir a otro participante",
                    "Repetir la prueba", "Explicar sus respuestas"))
        )
    ),

    Capsula(
        id = "disonancia",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Te gusta más algo si te pagan poco por defenderlo?",
        texto = "Festinger y Carlsmith pusieron a unos estudiantes a hacer durante una " +
            "hora una tarea insufriblemente aburrida: girar clavijas de un tablero, " +
            "una y otra vez. Al terminar les pedían un favor: decirle al siguiente " +
            "participante que la tarea había sido entretenida. A unos les pagaban un " +
            "dólar por mentir y a otros veinte. Después les preguntaban en privado qué " +
            "les había parecido de verdad. Lo esperable sería que los mejor pagados " +
            "dijeran más cosas buenas. Ocurrió lo contrario: los del dólar fueron los " +
            "que declararon haberse divertido más. Con veinte dólares tenías una " +
            "excusa para mentir. Con uno no, así que había que creérselo.",
        fuente = "Festinger y Carlsmith, Stanford (1959)",
        preguntas = listOf(
            Pregunta("¿En qué consistía la tarea aburrida?",
                listOf("Girar clavijas de un tablero", "Copiar listas de números",
                    "Ordenar fichas por color", "Escuchar un metrónomo")),
            Pregunta("¿Cuánto cobraban los dos grupos?",
                listOf("Un dólar y veinte dólares", "Nada y diez dólares",
                    "Cinco y cincuenta", "Lo mismo los dos")),
            Pregunta("¿Quiénes dijeron haberse divertido más?",
                listOf("Los que cobraron un dólar", "Los que cobraron veinte",
                    "Los dos grupos igual", "Ninguno de los dos"))
        )
    ),

    Capsula(
        id = "wason",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Buscas confirmar lo que crees o desmentirlo?",
        texto = "Peter Wason daba a los participantes una serie de tres números —dos, " +
            "cuatro, seis— y les decía que seguía una regla que debían adivinar. " +
            "Podían proponer todas las series que quisieran, y él respondía solo si " +
            "cumplían la regla o no. Casi todos hacían lo mismo: pensaban «van de dos " +
            "en dos» y proponían ocho-diez-doce, luego veinte-veintidós-veinticuatro, " +
            "y como todas les daban un sí, anunciaban su respuesta con seguridad. Casi " +
            "nadie probaba una serie que esperaba que fallara. La regla real era mucho " +
            "más amplia: simplemente tres números de menor a mayor. Para descubrirla " +
            "había que intentar refutar la propia hipótesis, y eso casi nadie lo hace " +
            "espontáneamente.",
        fuente = "Peter Wason, tarea 2-4-6 (1960)",
        preguntas = listOf(
            Pregunta("¿Cuál era la regla verdadera?",
                listOf("Tres números de menor a mayor", "Números pares consecutivos",
                    "Que sumen un múltiplo de seis", "Que empiecen por dos")),
            Pregunta("¿Qué contestaba Wason a cada propuesta?",
                listOf("Solo si cumplía la regla o no", "La regla completa",
                    "Cuántos aciertos llevaban", "Nada hasta el final")),
            Pregunta("¿Qué casi nadie hacía?",
                listOf("Proponer una serie que esperaban que fallara",
                    "Proponer más de tres series", "Preguntar por la regla",
                    "Escribir sus respuestas"))
        )
    ),

    Capsula(
        id = "espectador",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Ayudarías más si hay mucha gente alrededor o menos?",
        texto = "Menos. Darley y Latané montaron una emergencia fingida: los " +
            "participantes hablaban por interfono desde cabinas separadas y uno de " +
            "ellos, que era un actor, simulaba un ataque epiléptico. Cuando el sujeto " +
            "creía ser el único que lo oía, salía a ayudar en la inmensa mayoría de " +
            "los casos. Cuando creía que había otras cuatro personas escuchando, la " +
            "cifra se desplomaba y los que ayudaban tardaban mucho más. La " +
            "responsabilidad se reparte hasta diluirse. Conviene un apunte: el caso de " +
            "Kitty Genovese, del que se suele decir que treinta y ocho vecinos vieron " +
            "el crimen sin hacer nada, resultó estar bastante exagerado por la prensa. " +
            "El efecto, en cambio, se ha replicado muchas veces.",
        fuente = "Darley y Latané (1968); revisión del caso Genovese, Manning y otros (2007)",
        preguntas = listOf(
            Pregunta("¿Cómo se comunicaban los participantes?",
                listOf("Por interfono desde cabinas separadas", "En una misma sala",
                    "Por carta", "Por teléfono desde casa")),
            Pregunta("¿Qué simulaba el actor?",
                listOf("Un ataque epiléptico", "Un robo", "Un incendio", "Un desmayo por hambre")),
            Pregunta("¿Qué se dice del caso Kitty Genovese?",
                listOf("Que la prensa lo exageró bastante",
                    "Que fue el primer experimento", "Que nunca ocurrió",
                    "Que confirmó el efecto con precisión"))
        )
    ),

    Capsula(
        id = "loftus",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Puede una sola palabra cambiarte un recuerdo?",
        texto = "Elizabeth Loftus enseñó a unos participantes la grabación de un " +
            "accidente de tráfico y después les preguntó a qué velocidad iban los " +
            "coches. A unos les preguntó cuando «chocaron»; a otros, cuando «se " +
            "estrellaron». Los del verbo fuerte dieron velocidades bastante más altas, " +
            "aunque habían visto exactamente el mismo vídeo. Lo revelador vino una " +
            "semana más tarde, cuando les preguntó si habían visto cristales rotos en " +
            "la escena. No los había. Los que habían oído «se estrellaron» dijeron que " +
            "sí más del doble de veces que los otros. La pregunta no había medido el " +
            "recuerdo: lo había modificado.",
        fuente = "Loftus y Palmer (1974)",
        preguntas = listOf(
            Pregunta("¿Qué se les preguntó una semana después?",
                listOf("Si habían visto cristales rotos", "De qué color eran los coches",
                    "Cuántas personas iban dentro", "Si había llovido")),
            Pregunta("¿Había cristales rotos en el vídeo?",
                listOf("No había ninguno", "Sí, muchos", "Solo en un coche",
                    "No se veía bien")),
            Pregunta("¿Qué grupo dio velocidades más altas?",
                listOf("El del verbo «se estrellaron»", "El del verbo «chocaron»",
                    "Los dos igual", "El que vio el vídeo dos veces"))
        )
    ),

    Capsula(
        id = "asch",
        categoria = TemaReto.PSICOLOGIA,
        gancho = "¿Dirías que dos líneas iguales miden distinto si todos lo dicen?",
        texto = "Solomon Asch enseñaba una línea de referencia y tres líneas de " +
            "comparación, una de ellas claramente idéntica. La tarea era tan fácil que " +
            "a solas casi nadie fallaba. El truco estaba en la sala: todos los demás " +
            "participantes eran cómplices, y en determinadas rondas daban en voz alta " +
            "la misma respuesta equivocada, uno detrás de otro, antes de que llegara el " +
            "turno del sujeto real. Alrededor de tres de cada cuatro personas se " +
            "sumaron al error al menos una vez. Muchas dijeron después que veían " +
            "perfectamente la línea correcta, pero prefirieron no ser los raros del " +
            "grupo. Bastaba con que un solo cómplice diera la respuesta buena para que " +
            "la conformidad se hundiera.",
        fuente = "Solomon Asch, experimentos de conformidad (1951)",
        preguntas = listOf(
            Pregunta("¿Qué había que comparar?",
                listOf("Líneas", "Colores", "Sonidos", "Pesos")),
            Pregunta("¿Qué proporción se sumó al error alguna vez?",
                listOf("Unas tres de cada cuatro personas", "Una de cada diez",
                    "Todas sin excepción", "Menos de la mitad")),
            Pregunta("¿Qué bastaba para que la conformidad se hundiera?",
                listOf("Que un solo cómplice diera la respuesta buena",
                    "Que la sala fuera más pequeña", "Que respondieran por escrito",
                    "Que hubiera un premio"))
        )
    )
,

    // ------------------------------------------- más cuerpo y astronomía ---

    Capsula(
        id = "microbios",
        categoria = TemaReto.CUERPO,
        gancho = "¿Cuántas células tuyas hay en tu cuerpo?",
        texto = "Menos de las que crees, en proporción. Durante décadas se repitió que " +
            "llevamos diez bacterias por cada célula propia, una cifra que venía de una " +
            "estimación de los años setenta hecha a ojo. En 2016, Sender, Fuchs y Milo " +
            "rehicieron las cuentas con datos reales y les salió algo mucho más " +
            "modesto: alrededor de treinta y ocho billones de bacterias frente a unos " +
            "treinta billones de células humanas. Es decir, una proporción cercana al " +
            "uno por uno. La mayoría de esas bacterias viven en el intestino grueso, y " +
            "todas juntas pesan poco: en torno a doscientos gramos, menos que el " +
            "corazón.",
        fuente = "Sender, Fuchs y Milo, PLOS Biology (2016)",
        preguntas = listOf(
            Pregunta("¿Qué proporción se creía antes?",
                listOf("Diez bacterias por cada célula propia",
                    "Una por cada cien células", "Dos por cada tres",
                    "Cien por cada célula")),
            Pregunta("¿Dónde vive la mayoría de esas bacterias?",
                listOf("En el intestino grueso", "En la piel",
                    "En la boca", "En los pulmones")),
            Pregunta("¿Cuánto pesan todas juntas, aproximadamente?",
                listOf("Unos doscientos gramos", "Unos cinco kilos",
                    "Menos de un gramo", "Unos dos kilos"))
        )
    ),

    Capsula(
        id = "bostezo",
        categoria = TemaReto.CUERPO,
        gancho = "¿Por qué bostezas, y por qué se contagia?",
        texto = "No es por falta de oxígeno: se comprobó hace décadas que respirar aire " +
            "con más oxígeno no reduce los bostezos, y respirar aire con más dióxido " +
            "de carbono tampoco los aumenta. La hipótesis con más apoyo hoy es que el " +
            "bostezo enfría el cerebro. La inspiración profunda y el estiramiento de " +
            "la mandíbula aumentan el flujo de sangre y de aire fresco hacia la " +
            "cabeza, y encaja con que bostecemos más cuando la temperatura ambiente es " +
            "algo menor que la corporal, y casi nada cuando hace mucho calor. El " +
            "contagio es otra cosa: aparece hacia los cuatro o cinco años, es más " +
            "fuerte con personas cercanas y se ha observado también entre chimpancés y " +
            "perros.",
        fuente = "Investigación sobre termorregulación del bostezo; Provine, Gallup",
        preguntas = listOf(
            Pregunta("¿Qué hipótesis tiene hoy más apoyo?",
                listOf("Que el bostezo enfría el cerebro",
                    "Que compensa la falta de oxígeno",
                    "Que estira los pulmones", "Que avisa de sueño a los demás")),
            Pregunta("¿A qué edad aparece el contagio?",
                listOf("Hacia los cuatro o cinco años", "Desde el nacimiento",
                    "En la adolescencia", "Solo en adultos")),
            Pregunta("¿Cuándo se bosteza casi nada?",
                listOf("Cuando hace mucho calor", "Cuando hace mucho frío",
                    "Por la mañana", "Después de comer"))
        )
    ),

    Capsula(
        id = "puntociego",
        categoria = TemaReto.CUERPO,
        gancho = "¿Sabes que tienes un agujero en la visión y no lo notas?",
        texto = "En cada ojo hay un punto donde el nervio óptico atraviesa la retina " +
            "para salir hacia el cerebro. En esa zona no hay células sensibles a la " +
            "luz, así que es literalmente ciega: un agujero de unos cinco grados en tu " +
            "campo visual. No lo ves porque el cerebro lo rellena, inventando lo que " +
            "debería haber ahí a partir de lo que rodea al agujero. Se descubrió en el " +
            "siglo XVII: el físico Edme Mariotte lo demostró ante la corte inglesa " +
            "haciendo que dos cortesanos se miraran de cierta manera hasta que a cada " +
            "uno le desaparecía la cabeza del otro. El punto de cada ojo cae en un " +
            "sitio distinto, así que con los dos abiertos ni siquiera hace falta " +
            "rellenar.",
        fuente = "Edme Mariotte (1660); fisiología de la retina",
        preguntas = listOf(
            Pregunta("¿Por qué esa zona es ciega?",
                listOf("Porque ahí sale el nervio óptico y no hay células sensibles",
                    "Porque la cubre el párpado", "Porque está demasiado lejos del cristalino",
                    "Porque la sangre la tapa")),
            Pregunta("¿Quién lo demostró ante la corte inglesa?",
                listOf("Edme Mariotte", "Isaac Newton", "Robert Hooke", "Christiaan Huygens")),
            Pregunta("¿Por qué no lo notas normalmente?",
                listOf("El cerebro rellena el hueco", "Los ojos se mueven muy rápido",
                    "El agujero es diminuto", "Solo aparece de noche"))
        )
    ),

    Capsula(
        id = "huesos2",
        categoria = TemaReto.CUERPO,
        gancho = "¿De qué está hecho un hueso?",
        texto = "De dos cosas que por separado no servirían. Por un lado colágeno, una " +
            "proteína flexible que aguanta la tracción; por otro cristales de un " +
            "mineral de calcio y fósforo llamado hidroxiapatita, que aporta la dureza. " +
            "El colágeno solo sería una goma y el mineral solo sería una tiza: juntos " +
            "dan un material que resiste más peso que el hormigón siendo mucho más " +
            "ligero. Y no es una estructura muerta: hay células que destruyen hueso " +
            "viejo y otras que fabrican hueso nuevo todo el tiempo, en un recambio " +
            "constante. Se estima que el esqueleto adulto se renueva por completo cada " +
            "diez años aproximadamente.",
        fuente = "Histología ósea, valores de referencia",
        preguntas = listOf(
            Pregunta("¿Cómo se llama el mineral que da dureza al hueso?",
                listOf("Hidroxiapatita", "Queratina", "Silicato", "Aragonito")),
            Pregunta("¿Qué aporta el colágeno?",
                listOf("Flexibilidad frente a la tracción", "Dureza",
                    "Color", "Impermeabilidad")),
            Pregunta("¿Cada cuánto se renueva el esqueleto adulto?",
                listOf("Cada diez años aproximadamente", "Cada año",
                    "Cada cincuenta años", "No se renueva"))
        )
    ),

    Capsula(
        id = "tripas",
        categoria = TemaReto.CUERPO,
        gancho = "¿Por qué te suenan las tripas cuando tienes hambre?",
        texto = "El ruido no lo hace el hambre: lo hace la limpieza. Entre comida y " +
            "comida, cuando el intestino lleva un rato vacío, se pone en marcha una " +
            "onda de contracciones que barre de arriba abajo los restos que quedaron, " +
            "las bacterias y el moco. Los médicos la llaman complejo motor migratorio " +
            "y arranca aproximadamente cada hora y media si no comes nada. El sonido " +
            "es el aire y los líquidos moviéndose por un tubo vacío; con comida dentro " +
            "el ruido queda amortiguado y no se oye. Su nombre técnico es borborigmo, " +
            "una de esas palabras que suenan a lo que nombran.",
        fuente = "Fisiología digestiva: complejo motor migratorio",
        preguntas = listOf(
            Pregunta("¿Cómo se llama esa onda de contracciones?",
                listOf("Complejo motor migratorio", "Reflejo gastrocólico",
                    "Peristalsis inversa", "Ciclo entérico")),
            Pregunta("¿Cada cuánto arranca si no comes?",
                listOf("Aproximadamente cada hora y media", "Cada cinco minutos",
                    "Cada seis horas", "Solo por la noche")),
            Pregunta("¿Cómo se llama técnicamente ese ruido?",
                listOf("Borborigmo", "Estertor", "Crepitación", "Murmullo vesicular"))
        )
    ),

    Capsula(
        id = "venus",
        categoria = TemaReto.ESPACIO,
        gancho = "¿En qué planeta el día dura más que el año?",
        texto = "En Venus. Tarda unos doscientos cuarenta y tres días terrestres en dar " +
            "una vuelta sobre sí mismo, y solo doscientos veinticinco en dar la vuelta " +
            "al Sol. Su día es más largo que su año. Encima gira al revés que casi " +
            "todos los demás planetas, así que allí el Sol sale por el oeste. Y no lo " +
            "verías salir: una capa permanente de nubes de ácido sulfúrico lo tapa " +
            "todo. Abajo la presión es como estar a novecientos metros bajo el mar y " +
            "la temperatura pasa de los cuatrocientos sesenta grados, suficiente para " +
            "fundir plomo. Las sondas soviéticas que consiguieron posarse allí " +
            "aguantaron poco más de una hora antes de morir aplastadas.",
        fuente = "NASA, ficha planetaria de Venus",
        preguntas = listOf(
            Pregunta("¿Cuánto dura un día en Venus?",
                listOf("Unos 243 días terrestres", "Unos 24 días terrestres",
                    "Unas 30 horas", "Unos 700 días terrestres")),
            Pregunta("¿De qué están hechas sus nubes?",
                listOf("De ácido sulfúrico", "De vapor de agua",
                    "De polvo de hierro", "De metano")),
            Pregunta("¿Cuánto aguantaron las sondas que se posaron?",
                listOf("Poco más de una hora", "Varios meses",
                    "Unos segundos", "Tres semanas"))
        )
    ),

    Capsula(
        id = "anioluz",
        categoria = TemaReto.ESPACIO,
        gancho = "¿Un año luz es tiempo o distancia?",
        texto = "Distancia, aunque el nombre despiste. Es lo que recorre la luz en un " +
            "año: unos nueve billones y medio de kilómetros. La estrella más cercana " +
            "al Sol, Próxima Centauri, está a cuatro coma dos años luz, lo que " +
            "significa que su luz sale, viaja durante más de cuatro años y llega aquí " +
            "cuando ya no es del todo actual. Mirar el cielo es mirar el pasado, y cada " +
            "punto lo es en distinta medida: la Luna la ves con algo más de un segundo " +
            "de retraso, el Sol con ocho minutos, y algunas galaxias tal como eran " +
            "cuando la Tierra ni existía. La sonda Voyager 1, el objeto más rápido que " +
            "hemos lanzado lejos, tardaría unos setenta mil años en cubrir un año luz.",
        fuente = "Definición de la Unión Astronómica Internacional; datos de la NASA",
        preguntas = listOf(
            Pregunta("¿A qué distancia está Próxima Centauri?",
                listOf("A 4,2 años luz", "A 42 años luz", "A 0,4 años luz", "A 140 años luz")),
            Pregunta("¿Con cuánto retraso ves la Luna?",
                listOf("Algo más de un segundo", "Ocho minutos",
                    "Una hora", "Instantáneamente")),
            Pregunta("¿Cuánto tardaría la Voyager 1 en recorrer un año luz?",
                listOf("Unos setenta mil años", "Unos cien años",
                    "Unos mil años", "Unos diez millones de años"))
        )
    ),

    Capsula(
        id = "luna",
        categoria = TemaReto.ESPACIO,
        gancho = "¿Por qué la Luna siempre nos enseña la misma cara?",
        texto = "Porque tarda exactamente lo mismo en girar sobre sí misma que en dar la " +
            "vuelta a la Tierra. No es casualidad: la gravedad terrestre lleva miles de " +
            "millones de años frenando su rotación hasta dejarla sincronizada, un " +
            "fenómeno llamado acoplamiento de marea que también le ha pasado a muchas " +
            "lunas del sistema solar. La cara oculta no la vio nadie hasta 1959, " +
            "cuando la sonda soviética Luna 3 la fotografió y sorprendió a todos: " +
            "tiene muchísimos más cráteres y casi ninguna de las grandes llanuras " +
            "oscuras que sí se ven desde aquí. Y no está quieta: la Luna se aleja de " +
            "nosotros unos tres centímetros y ocho milímetros cada año.",
        fuente = "NASA; mediciones láser desde los reflectores del programa Apolo",
        preguntas = listOf(
            Pregunta("¿Cómo se llama el fenómeno que la sincronizó?",
                listOf("Acoplamiento de marea", "Resonancia orbital",
                    "Precesión de los equinoccios", "Deriva sideral")),
            Pregunta("¿Qué sonda fotografió la cara oculta y en qué año?",
                listOf("La Luna 3, en 1959", "El Apolo 8, en 1968",
                    "El Sputnik, en 1957", "La Ranger 7, en 1964")),
            Pregunta("¿Cuánto se aleja la Luna cada año?",
                listOf("Unos 3,8 centímetros", "Unos 3,8 metros",
                    "Unos 38 centímetros", "No se aleja"))
        )
    ),

    Capsula(
        id = "apolo13",
        categoria = TemaReto.ESPACIO,
        gancho = "¿Sabes cómo se salvó la tripulación del Apolo 13?",
        texto = "Con cinta adhesiva, entre otras cosas. A los dos días de vuelo estalló " +
            "un tanque de oxígeno y el módulo de mando quedó inservible. Los tres " +
            "astronautas se refugiaron en el módulo lunar, diseñado para dos personas " +
            "durante dos días, y tuvieron que estirarlo para tres durante cuatro. El " +
            "problema más urgente fue el dióxido de carbono: los filtros del módulo " +
            "lunar se saturaban, y los de repuesto del módulo de mando eran cuadrados " +
            "mientras que los huecos donde debían encajar eran redondos. En Houston " +
            "vaciaron sobre una mesa una copia exacta de todo lo que había a bordo y " +
            "montaron un adaptador con cinta, una bolsa de plástico, un calcetín y la " +
            "tapa de un manual. Volvieron los tres vivos.",
        fuente = "NASA, informe de la misión Apolo 13 (1970)",
        preguntas = listOf(
            Pregunta("¿Qué forma tenían los filtros que sobraban?",
                listOf("Cuadrados, y los huecos redondos", "Redondos, y los huecos cuadrados",
                    "Todos redondos", "Todos cuadrados")),
            Pregunta("¿Para cuántas personas y días estaba diseñado el módulo lunar?",
                listOf("Para dos personas y dos días", "Para tres personas y cuatro días",
                    "Para una persona y una semana", "Para cuatro personas y un día")),
            Pregunta("¿Qué usaron para montar el adaptador?",
                listOf("Cinta, una bolsa, un calcetín y la tapa de un manual",
                    "Piezas de repuesto del propio filtro", "Un traje espacial cortado",
                    "Nada: apagaron el sistema"))
        )
    ),

    Capsula(
        id = "cavendish",
        categoria = TemaReto.ESPACIO,
        gancho = "¿Cómo se pesa la Tierra si no cabe en ninguna balanza?",
        texto = "Henry Cavendish lo consiguió en 1798 sin salir de un cobertizo. Colgó " +
            "de un hilo finísimo una barra horizontal con dos bolas pequeñas de plomo " +
            "en los extremos, y acercó por fuera dos bolas mucho mayores. La atracción " +
            "gravitatoria entre unas y otras hacía girar la barra un poquito, " +
            "retorciendo el hilo; midiendo ese giro minúsculo se podía calcular la " +
            "fuerza. El montaje era tan delicado que Cavendish lo manejaba desde otra " +
            "habitación con un telescopio, para que ni el calor de su cuerpo lo " +
            "alterara. De ahí salió la densidad de la Tierra, y con ella su masa. Su " +
            "resultado se queda a un uno por ciento del valor que manejamos hoy.",
        fuente = "Henry Cavendish, «Experiments to Determine the Density of the Earth» (1798)",
        preguntas = listOf(
            Pregunta("¿De qué eran las bolas del experimento?",
                listOf("De plomo", "De hierro", "De piedra", "De cristal")),
            Pregunta("¿Por qué manejaba el montaje desde otra habitación?",
                listOf("Para que ni el calor de su cuerpo lo alterara",
                    "Porque era peligroso", "Porque no cabía dentro",
                    "Para que nadie viera el método")),
            Pregunta("¿Qué precisión alcanzó frente al valor actual?",
                listOf("Se queda a un uno por ciento", "Se equivocó por diez veces",
                    "Acertó exactamente", "Se quedó a la mitad"))
        )
    )
,

    // ------------------------------------------------ más historia y arte ---

    Capsula(
        id = "cienanios",
        categoria = TemaReto.HISTORIA,
        gancho = "¿Cuánto duró la Guerra de los Cien Años?",
        texto = "Ciento dieciséis. Empezó en 1337 y terminó en 1453, y ni siquiera fue " +
            "una guerra continua: fueron rachas de campañas separadas por treguas " +
            "largas, algunas de décadas, entre las coronas de Inglaterra y Francia por " +
            "el trono francés. El nombre se lo pusieron historiadores del siglo XIX, " +
            "mucho después, agrupando conflictos que sus protagonistas no vivieron como " +
            "uno solo. Por el camino cambió la forma de hacer la guerra: el arco largo " +
            "inglés destrozó a la caballería pesada en Crécy y Azincourt, y hacia el " +
            "final aparecieron los cañones. Juana de Arco entra en escena en 1429, casi " +
            "un siglo después del comienzo.",
        fuente = "Historiografía de la Guerra de los Cien Años (1337-1453)",
        preguntas = listOf(
            Pregunta("¿Cuántos años duró exactamente?",
                listOf("Ciento dieciséis", "Cien justos", "Noventa y nueve", "Ciento treinta")),
            Pregunta("¿Quién le puso ese nombre?",
                listOf("Historiadores del siglo XIX", "Los propios combatientes",
                    "El papa de la época", "Los cronistas de Juana de Arco")),
            Pregunta("¿Qué arma destrozó a la caballería pesada?",
                listOf("El arco largo inglés", "El cañón", "La ballesta genovesa", "La pica suiza"))
        )
    ),

    Capsula(
        id = "antikythera",
        categoria = TemaReto.HISTORIA,
        gancho = "¿Sabes qué es el objeto más raro sacado del fondo del mar?",
        texto = "Unos buceadores de esponjas encontraron en 1901, junto a la isla griega " +
            "de Anticitera, los restos de un naufragio romano. Entre estatuas y " +
            "ánforas apareció un bulto de bronce corroído del tamaño de una caja de " +
            "zapatos. Tardaron décadas en entender qué era, hasta que las radiografías " +
            "revelaron dentro más de treinta engranajes encajados con una precisión " +
            "que nadie esperaba de la Antigüedad. Es una calculadora astronómica: " +
            "girando una manivela predecía las posiciones del Sol y la Luna, las fases " +
            "lunares y los eclipses, e incluso llevaba la cuenta de los años olímpicos. " +
            "No se conoce nada de complejidad comparable hasta los relojes " +
            "astronómicos europeos, mil cuatrocientos años más tarde.",
        fuente = "Mecanismo de Anticitera; estudios de Freeth y otros en Nature",
        preguntas = listOf(
            Pregunta("¿Quiénes lo encontraron y en qué año?",
                listOf("Buceadores de esponjas, en 1901", "Arqueólogos, en 1955",
                    "Pescadores, en 1830", "Militares, en 1912")),
            Pregunta("¿Qué revelaron las radiografías?",
                listOf("Más de treinta engranajes encajados", "Una inscripción en latín",
                    "Monedas dentro de una caja", "Que estaba hueco")),
            Pregunta("¿Cuánto tardó en aparecer algo comparable?",
                listOf("Unos mil cuatrocientos años", "Unos cincuenta años",
                    "Unos trescientos años", "Nunca ha vuelto a aparecer"))
        )
    ),

    Capsula(
        id = "cuarentena",
        categoria = TemaReto.HISTORIA,
        gancho = "¿De dónde viene la palabra «cuarentena»?",
        texto = "De cuarenta días, y la cifra tiene historia. En 1377, la ciudad de " +
            "Ragusa —hoy Dubrovnik— aprobó una medida pionera contra la peste: los " +
            "barcos y viajeros que llegaran de zonas infectadas debían esperar " +
            "aislados en un islote antes de entrar. El plazo original era de treinta " +
            "días, y lo llamaban trentino. Al comprobar que no bastaba lo ampliaron a " +
            "cuarenta, quarantino, y ese nombre es el que se quedó en media Europa. " +
            "Nadie sabía entonces qué causaba la enfermedad; ni bacterias ni pulgas ni " +
            "ratas estaban en el mapa. Lo que sí habían observado es que el tiempo de " +
            "espera funcionaba, y con eso les bastó.",
        fuente = "Archivo de Ragusa (1377); historia de la salud pública",
        preguntas = listOf(
            Pregunta("¿Qué ciudad aprobó la medida en 1377?",
                listOf("Ragusa, hoy Dubrovnik", "Venecia", "Génova", "Marsella")),
            Pregunta("¿Cuál era el plazo original?",
                listOf("Treinta días", "Cuarenta días", "Siete días", "Noventa días")),
            Pregunta("¿Qué sabían sobre la causa de la enfermedad?",
                listOf("Nada: ni bacterias ni pulgas ni ratas",
                    "Que la transmitían las ratas", "Que era cosa del agua",
                    "Que venía del aire de los pantanos"))
        )
    ),

    Capsula(
        id = "jenner",
        categoria = TemaReto.HISTORIA,
        gancho = "¿Sabes por qué se llama «vacuna»?",
        texto = "Por las vacas. Edward Jenner se fijó en algo que en el campo inglés se " +
            "comentaba: las lecheras que habían pasado la viruela de las vacas, una " +
            "enfermedad leve, no cogían la viruela humana, que mataba a uno de cada " +
            "tres contagiados y desfiguraba al resto. En 1796 tomó material de una " +
            "pústula de la mano de una lechera llamada Sarah Nelmes y se lo inoculó a " +
            "James Phipps, el hijo de ocho años de su jardinero. El niño pasó unos días " +
            "de malestar. Semanas después Jenner le inoculó viruela humana, y no " +
            "enfermó. De variolae vaccinae, viruela de la vaca, salió la palabra que " +
            "usamos hoy para todas.",
        fuente = "Edward Jenner (1796); erradicación de la viruela declarada en 1980",
        preguntas = listOf(
            Pregunta("¿Cómo se llamaba la lechera?",
                listOf("Sarah Nelmes", "Mary Wortley", "Anne Phipps", "Elizabeth Gray")),
            Pregunta("¿Qué edad tenía James Phipps?",
                listOf("Ocho años", "Dos años", "Quince años", "Veinte años")),
            Pregunta("¿A cuántos contagiados mataba la viruela humana?",
                listOf("A uno de cada tres", "A uno de cada cien",
                    "A nueve de cada diez", "A uno de cada mil"))
        )
    ),

    Capsula(
        id = "magallanes",
        categoria = TemaReto.HISTORIA,
        gancho = "De los que salieron a dar la vuelta al mundo, ¿cuántos volvieron?",
        texto = "Dieciocho, de unos doscientos setenta. La expedición zarpó de Sevilla " +
            "en 1519 con cinco naves al mando de Magallanes, buscando una ruta a las " +
            "Molucas por el oeste. Tres años después regresó una sola nave, la " +
            "Victoria, al mando de Juan Sebastián Elcano, con dieciocho hombres " +
            "famélicos a bordo. Magallanes había muerto en Filipinas. Al llegar " +
            "descubrieron algo que nadie había previsto: su diario iba un día " +
            "atrasado respecto al calendario de tierra. Habían navegado siempre hacia " +
            "el oeste, persiguiendo el sol, y habían perdido un día entero por el " +
            "camino. Fue la primera prueba práctica de lo que hoy resolvemos con la " +
            "línea internacional de cambio de fecha.",
        fuente = "Crónica de Antonio Pigafetta; expedición Magallanes-Elcano (1519-1522)",
        preguntas = listOf(
            Pregunta("¿Cómo se llamaba la nave que volvió?",
                listOf("La Victoria", "La Trinidad", "La Concepción", "La Santiago")),
            Pregunta("¿Qué descubrieron al comparar su diario con el calendario?",
                listOf("Que llevaban un día de atraso", "Que llevaban un día de adelanto",
                    "Que coincidía exactamente", "Que faltaba un mes entero")),
            Pregunta("¿Dónde murió Magallanes?",
                listOf("En Filipinas", "En el estrecho que lleva su nombre",
                    "En las Molucas", "En el viaje de vuelta"))
        )
    ),

    Capsula(
        id = "monalisa",
        categoria = TemaReto.ARTE,
        gancho = "¿Sabes qué hizo famosa a la Mona Lisa?",
        texto = "Un robo. Hasta 1911 era un cuadro apreciado pero no especialmente " +
            "célebre, uno más entre las joyas del Louvre. Aquel agosto, un empleado " +
            "italiano llamado Vincenzo Peruggia se escondió dentro del museo, esperó a " +
            "que cerrara, descolgó el cuadro, lo sacó bajo la ropa y se fue andando. " +
            "Nadie notó su ausencia hasta el día siguiente. Durante los dos años que " +
            "estuvo desaparecida, la prensa del mundo entero publicó su imagen a " +
            "diario, y al hueco vacío de la pared acudió más gente de la que había ido " +
            "nunca a ver el cuadro. Peruggia fue detenido al intentar venderla en " +
            "Florencia: dijo que la devolvía a Italia.",
        fuente = "Robo del Louvre (1911) y detención de Peruggia (1913)",
        preguntas = listOf(
            Pregunta("¿Cómo sacó Peruggia el cuadro?",
                listOf("Bajo la ropa, andando", "Por una ventana con cuerdas",
                    "En un carro de basura", "Escondido en un cajón de obras")),
            Pregunta("¿Cuánto tiempo estuvo desaparecida?",
                listOf("Dos años", "Dos semanas", "Diez años", "Seis meses")),
            Pregunta("¿Dónde lo detuvieron?",
                listOf("En Florencia, al intentar venderla", "En París, en la estación",
                    "En Roma, en su casa", "En la frontera suiza"))
        )
    ),

    Capsula(
        id = "sixtina",
        categoria = TemaReto.ARTE,
        gancho = "¿Miguel Ángel pintó la Capilla Sixtina tumbado?",
        texto = "No: de pie, con la cabeza echada hacia atrás y el brazo en alto, sobre " +
            "un andamio que él mismo diseñó cuando rechazó el que le habían montado. " +
            "La imagen del pintor acostado viene de una película. Tardó cuatro años, " +
            "de 1508 a 1512, para unos trescientos metros cuadrados y más de " +
            "trescientas figuras. Lo aceptó a regañadientes: se consideraba escultor, " +
            "no pintor, y sospechaba que el encargo era una maniobra de sus rivales " +
            "para verle fracasar. Escribió un soneto quejándose de que la postura le " +
            "había deformado el cuerpo y de que la pintura le caía en la cara. Al " +
            "descubrirse la bóveda, el estilo de la pintura europea cambió de golpe.",
        fuente = "Correspondencia y sonetos de Miguel Ángel; bóveda de la Sixtina (1508-1512)",
        preguntas = listOf(
            Pregunta("¿En qué postura pintó la bóveda?",
                listOf("De pie, con la cabeza hacia atrás", "Tumbado boca arriba",
                    "Sentado en una silla alta", "Colgado de arneses")),
            Pregunta("¿Cuántos años tardó?",
                listOf("Cuatro", "Diez", "Uno", "Veinte")),
            Pregunta("¿Cómo se consideraba a sí mismo?",
                listOf("Escultor, no pintor", "Pintor, no escultor",
                    "Arquitecto por encima de todo", "Poeta antes que nada"))
        )
    ),

    Capsula(
        id = "vantablack",
        categoria = TemaReto.ARTE,
        gancho = "¿Sabes que un artista compró el derecho exclusivo a un color?",
        texto = "El Vantablack es un material hecho de nanotubos de carbono que absorbe " +
            "más del noventa y nueve por ciento de la luz que le llega. Una superficie " +
            "cubierta con él deja de leerse como superficie: los pliegues y los " +
            "bordes desaparecen y el ojo solo ve un agujero. En 2016, el escultor " +
            "Anish Kapoor adquirió los derechos exclusivos para usarlo en arte, y el " +
            "mundillo se le echó encima. La respuesta más sonada vino del artista " +
            "Stuart Semple, que fabricó el rosa más intenso que pudo y lo puso a la " +
            "venta para cualquiera del planeta excepto para Kapoor, obligando a firmar " +
            "una declaración en la compra. Kapoor consiguió un bote igualmente y " +
            "publicó una foto con el dedo manchado.",
        fuente = "Surrey NanoSystems; disputa Kapoor-Semple (2016)",
        preguntas = listOf(
            Pregunta("¿De qué está hecho el Vantablack?",
                listOf("De nanotubos de carbono", "De pigmento de carbón vegetal",
                    "De óxido de hierro", "De cristal molido")),
            Pregunta("¿Qué fabricó Stuart Semple como respuesta?",
                listOf("El rosa más intenso que pudo", "Un negro todavía más oscuro",
                    "Un blanco absoluto", "Una pintura que cambia de color")),
            Pregunta("¿Qué porcentaje de luz absorbe el Vantablack?",
                listOf("Más del noventa y nueve por ciento", "Cerca del setenta",
                    "Justo la mitad", "El ochenta y cinco"))
        )
    ),

    Capsula(
        id = "docenotas",
        categoria = TemaReto.ARTE,
        gancho = "¿Por qué la música occidental tiene doce notas y no otra cantidad?",
        texto = "Por un problema que no tiene solución exacta. Si vas subiendo de quinta " +
            "en quinta —el intervalo más consonante después de la octava— das doce " +
            "pasos y llegas casi, casi, a la nota de partida. Casi: sobra un pelo, un " +
            "desajuste que los griegos ya conocían y que se llama coma pitagórica. " +
            "Durante siglos se repartió ese sobrante de mil maneras, afinando unos " +
            "intervalos perfectos a costa de dejar otros insufribles, lo que obligaba " +
            "a reafinar el instrumento al cambiar de tonalidad. La salida fue " +
            "repartir el error a partes iguales entre las doce notas: ninguna queda " +
            "perfecta pero todas quedan aceptables, y ya puedes tocar en cualquier " +
            "tono sin tocar una clavija.",
        fuente = "Acústica musical: coma pitagórica y temperamento igual",
        preguntas = listOf(
            Pregunta("¿Cómo se llama ese sobrante que no cuadra?",
                listOf("Coma pitagórica", "Intervalo de lobo",
                    "Serie armónica", "Cuarta aumentada")),
            Pregunta("¿Cuántos pasos de quinta hay que dar para volver casi al principio?",
                listOf("Doce", "Siete", "Cinco", "Veinticuatro")),
            Pregunta("¿Cuál fue la salida?",
                listOf("Repartir el error a partes iguales entre las doce notas",
                    "Eliminar dos notas", "Añadir cuatro notas más",
                    "Reafinar en cada pieza"))
        )
    ),

    Capsula(
        id = "pentimenti",
        categoria = TemaReto.ARTE,
        gancho = "¿Sabes que muchos cuadros esconden otro debajo?",
        texto = "Los llaman pentimenti, del italiano arrepentirse. Son los cambios que " +
            "el pintor hizo sobre la marcha y tapó con otra capa: un brazo en otra " +
            "posición, una figura eliminada, un fondo entero distinto. Durante siglos " +
            "solo se intuían cuando la capa de encima se volvía transparente con el " +
            "tiempo, porque el óleo gana translucidez al envejecer y deja asomar lo " +
            "que había abajo. Hoy se ven con reflectografía infrarroja, que atraviesa " +
            "la pintura y muestra el dibujo preparatorio, y con fluorescencia de rayos " +
            "X, que distingue qué pigmento hay en cada punto y permite reconstruir la " +
            "versión oculta en color. Bajo algunos cuadros muy conocidos ha aparecido " +
            "un retrato completamente distinto.",
        fuente = "Técnicas de examen científico de pintura: reflectografía IR y XRF",
        preguntas = listOf(
            Pregunta("¿Qué significa «pentimenti» en italiano?",
                listOf("Arrepentirse", "Pintar de nuevo", "Capa profunda", "Sombra oculta")),
            Pregunta("¿Por qué a veces asoman con el tiempo?",
                listOf("El óleo gana translucidez al envejecer",
                    "La pintura de encima se agrieta", "El barniz los disuelve",
                    "El lienzo se encoge")),
            Pregunta("¿Qué técnica permite reconstruir la versión oculta en color?",
                listOf("La fluorescencia de rayos X", "La reflectografía infrarroja",
                    "La luz ultravioleta", "La microscopía óptica"))
        )
    )
,

    // -------------------------------------------- más animales y lenguaje ---

    Capsula(
        id = "medusa",
        categoria = TemaReto.ANIMALES,
        gancho = "¿Sabes qué animal puede rejuvenecer en vez de morir?",
        texto = "La Turritopsis dohrnii, una medusa de unos cinco milímetros. Cuando " +
            "está herida, hambrienta o vieja, en lugar de morir hace algo insólito: " +
            "sus células se transforman en otro tipo de célula y el animal entero " +
            "revierte a la fase de pólipo, la etapa juvenil pegada al fondo desde la " +
            "que había crecido. Desde ahí vuelve a desarrollarse y a formar medusas de " +
            "nuevo. En teoría el ciclo podría repetirse indefinidamente, y por eso se " +
            "la llama la medusa inmortal, aunque el nombre engaña: en el mar la comen, " +
            "la aplastan y la enferman como a cualquier otra. Lo que no tiene es una " +
            "muerte por vejez inevitable.",
        fuente = "Biología de Turritopsis dohrnii; transdiferenciación celular",
        preguntas = listOf(
            Pregunta("¿A qué fase revierte?",
                listOf("A la de pólipo", "A la de huevo", "A la de larva nadadora",
                    "A una medusa más pequeña")),
            Pregunta("¿Cuánto mide, aproximadamente?",
                listOf("Unos cinco milímetros", "Unos veinte centímetros",
                    "Un metro", "Menos de una micra")),
            Pregunta("¿Por qué el nombre «inmortal» engaña?",
                listOf("Porque en el mar la comen y la enferman igual",
                    "Porque solo lo hace una vez", "Porque no está confirmado",
                    "Porque solo pasa en cautividad"))
        )
    ),

    Capsula(
        id = "delfines",
        categoria = TemaReto.ANIMALES,
        gancho = "¿Cómo duerme un animal que tiene que salir a respirar?",
        texto = "Con medio cerebro. Los delfines no respiran por reflejo como nosotros: " +
            "cada vez que suben a tomar aire es una decisión consciente, así que " +
            "quedarse dormidos del todo sería ahogarse. La solución es el sueño " +
            "unihemisférico: un hemisferio entra en ondas lentas mientras el otro " +
            "sigue despierto, y el ojo contrario al hemisferio dormido se cierra " +
            "mientras el otro permanece abierto. Cada par de horas se turnan. Así " +
            "descansan sin dejar de nadar, de vigilar depredadores ni de subir a la " +
            "superficie. Las crías recién nacidas y sus madres pasan además varias " +
            "semanas prácticamente sin dormir en el sentido en que lo entendemos " +
            "nosotros.",
        fuente = "Estudios sobre sueño unihemisférico en cetáceos",
        preguntas = listOf(
            Pregunta("¿Qué pasa con los ojos mientras duermen?",
                listOf("Se cierra el contrario al hemisferio dormido",
                    "Se cierran los dos", "Permanecen los dos abiertos",
                    "Se cierran alternándose cada segundo")),
            Pregunta("¿Cada cuánto se turnan los hemisferios?",
                listOf("Cada par de horas", "Cada pocos segundos",
                    "Una vez al día", "Cada semana")),
            Pregunta("¿Por qué no pueden dormirse del todo?",
                listOf("Porque respirar es una decisión consciente",
                    "Porque el agua está fría", "Porque nadan en grupo",
                    "Porque el oído no se apaga"))
        )
    ),

    Capsula(
        id = "flamencos",
        categoria = TemaReto.ANIMALES,
        gancho = "¿Sabes de qué color nacen los flamencos?",
        texto = "Grises. El rosa no lo llevan de fábrica: se lo comen. Su dieta está " +
            "llena de crustáceos diminutos y algas cargados de carotenoides, los " +
            "mismos pigmentos que dan color a las zanahorias, y el hígado los procesa " +
            "y los deposita en las plumas. Un flamenco en cautividad al que no se le " +
            "cuide la dieta se vuelve blanquecino. El tono llega a ser una señal " +
            "social: los ejemplares más intensos suelen ser los que mejor se " +
            "alimentan, y los que primero encuentran pareja. Los padres, además, " +
            "alimentan a las crías con una secreción del buche tan cargada de " +
            "pigmento que ellos mismos palidecen mientras las crían.",
        fuente = "Ornitología: carotenoides y coloración en flamencos",
        preguntas = listOf(
            Pregunta("¿De qué color nacen?",
                listOf("Grises", "Rosas pálidos", "Blancos puros", "Marrones")),
            Pregunta("¿Cómo se llaman los pigmentos que los tiñen?",
                listOf("Carotenoides", "Melaninas", "Antocianinas", "Porfirinas")),
            Pregunta("¿Qué les pasa a los padres mientras crían?",
                listOf("Palidecen", "Se vuelven más intensos",
                    "Pierden las plumas", "No cambian de color"))
        )
    ),

    Capsula(
        id = "cuervos",
        categoria = TemaReto.ANIMALES,
        gancho = "¿Sabes qué animal guarda rencor a una cara concreta?",
        texto = "El cuervo. John Marzluff, de la Universidad de Washington, se puso una " +
            "máscara de goma con un rostro humano para capturar y anillar unos cuervos " +
            "del campus, y después los soltó. Cada vez que alguien paseaba por allí con " +
            "esa máscara, los cuervos le graznaban y le acosaban en grupo; con " +
            "cualquier otra máscara, ni caso. Lo llamativo llegó con los años: la " +
            "reacción se extendió a cuervos que nunca habían sido capturados, incluidos " +
            "los que ni siquiera habían nacido entonces. Habían aprendido de los demás " +
            "quién era el peligroso. El rechazo a esa cara siguió apareciendo más de " +
            "una década después.",
        fuente = "John Marzluff, Universidad de Washington; experimentos con máscaras",
        preguntas = listOf(
            Pregunta("¿Qué llevaba puesto el investigador al capturarlos?",
                listOf("Una máscara de goma con un rostro humano", "Un traje naranja",
                    "Guantes de un color llamativo", "Nada especial")),
            Pregunta("¿Qué ocurrió con los años?",
                listOf("Reaccionaban cuervos que nunca fueron capturados",
                    "Los cuervos olvidaron la cara", "Solo la recordaban los capturados",
                    "Dejaron el campus")),
            Pregunta("¿Cuánto duró el rechazo a esa cara?",
                listOf("Más de una década", "Unas semanas", "Un par de años", "Una temporada"))
        )
    ),

    Capsula(
        id = "charran",
        categoria = TemaReto.ANIMALES,
        gancho = "¿Sabes qué animal ve más luz de sol al año?",
        texto = "El charrán ártico. Cría en el verano del Ártico y pasa el invierno " +
            "boreal en la Antártida, persiguiendo el verano de un polo al otro; recorre " +
            "del orden de noventa mil kilómetros cada año, y no en línea recta sino " +
            "haciendo eses enormes para aprovechar los vientos. A lo largo de una vida " +
            "de treinta años acumula distancia suficiente para haber ido y vuelto a la " +
            "Luna varias veces. Cómo se orientan las aves migratorias sigue sin estar " +
            "cerrado del todo, pero hay bastante apoyo para que perciban el campo " +
            "magnético terrestre mediante unas proteínas de la retina llamadas " +
            "criptocromos, que reaccionan a la luz azul. Es decir: verían el campo " +
            "magnético, más que sentirlo.",
        fuente = "Seguimiento por geolocalizadores del charrán ártico; hipótesis del criptocromo",
        preguntas = listOf(
            Pregunta("¿Cuántos kilómetros recorre al año, aproximadamente?",
                listOf("Unos noventa mil", "Unos nueve mil",
                    "Unos novecientos mil", "Unos mil")),
            Pregunta("¿Cómo se llaman las proteínas de la retina implicadas?",
                listOf("Criptocromos", "Rodopsinas", "Magnetitas", "Opsinas azules")),
            Pregunta("¿A qué color de luz reaccionan?",
                listOf("A la luz azul", "A la luz roja",
                    "A la infrarroja", "A la ultravioleta"))
        )
    ),

    Capsula(
        id = "okey",
        categoria = TemaReto.LENGUAJE,
        gancho = "¿De dónde viene la palabra «OK»?",
        texto = "De una broma de periódico. En 1839, en Boston, estaba de moda entre " +
            "redactores abreviar frases escritas mal a propósito. «All correct» se " +
            "escribía en broma «oll korrect», y de ahí salió OK, que apareció impreso " +
            "por primera vez en el Boston Morning Post aquel año. La moda habría " +
            "muerto sola si no llega a ser por la campaña electoral del año siguiente: " +
            "los partidarios de Martin Van Buren, natural de Kinderhook, montaron los " +
            "clubes OK por su apodo, Old Kinderhook, y las dos letras se repitieron " +
            "hasta clavarse en el idioma. Hoy es una de las palabras más reconocibles " +
            "del planeta, y casi nadie sabe que empezó siendo una falta de ortografía " +
            "de chiste.",
        fuente = "Investigación etimológica de Allen Walker Read (1963-1964)",
        preguntas = listOf(
            Pregunta("¿Qué frase mal escrita hay detrás de OK?",
                listOf("«Oll korrect»", "«Oll klear», por all clear",
                    "«Okay dokey»", "«Order kept»")),
            Pregunta("¿Cuál era el apodo de Martin Van Buren?",
                listOf("Old Kinderhook", "Old Kentucky", "Old Keeper", "Original King")),
            Pregunta("¿En qué periódico apareció impreso por primera vez?",
                listOf("El Boston Morning Post", "El New York Herald",
                    "El Philadelphia Ledger", "El Times de Londres"))
        )
    ),

    Capsula(
        id = "guugu",
        categoria = TemaReto.LENGUAJE,
        gancho = "¿Sabes que hay lenguas sin palabras para izquierda y derecha?",
        texto = "El guugu yimithirr, del norte de Australia, es una de ellas. Para " +
            "situar cualquier cosa usa siempre los puntos cardinales: no dices que " +
            "tienes una taza a la izquierda, dices que la tienes al norte; y a alguien " +
            "no le pides que se aparte a un lado, sino que se mueva hacia el oeste. " +
            "Esto obliga a sus hablantes a mantener la orientación permanentemente " +
            "activa, y lo hacen: puestos a ciegas en un sitio desconocido señalan el " +
            "norte sin dudar, algo que a un europeo se le da fatal. El lingüista " +
            "Stephen Levinson lo documentó en los años ochenta y noventa, y con ello " +
            "abrió una discusión larga sobre hasta qué punto la lengua que hablas " +
            "moldea cómo percibes el espacio.",
        fuente = "Stephen Levinson, trabajo de campo sobre el guugu yimithirr",
        preguntas = listOf(
            Pregunta("¿Dónde se habla el guugu yimithirr?",
                listOf("En el norte de Australia", "En el Amazonas",
                    "En Papúa Nueva Guinea", "En el norte de Canadá")),
            Pregunta("¿Cómo pides a alguien que se aparte?",
                listOf("Que se mueva hacia un punto cardinal", "Señalando con la mano",
                    "Diciendo «hacia allá»", "Con un chasquido")),
            Pregunta("¿Qué se les da fatal a los europeos en comparación?",
                listOf("Señalar el norte en un sitio desconocido",
                    "Aprender la lengua", "Distinguir colores",
                    "Contar objetos lejanos"))
        )
    ),

    Capsula(
        id = "lenguas",
        categoria = TemaReto.LENGUAJE,
        gancho = "¿Cuántas lenguas se hablan hoy en el mundo?",
        texto = "Alrededor de siete mil, aunque la cifra exacta depende de dónde pongas " +
            "la frontera entre lengua y dialecto, que es una decisión más política que " +
            "científica. El reparto es brutalmente desigual: cerca de la mitad de la " +
            "humanidad habla como lengua materna una de las veinte más extendidas, y " +
            "en el otro extremo hay centenares de lenguas con menos de mil hablantes, " +
            "casi todos ancianos. La estimación habitual es que a lo largo de este " +
            "siglo podría desaparecer entre un tercio y la mitad de todas ellas. Con " +
            "cada una se va una gramática distinta, una forma propia de trocear el " +
            "mundo, y casi siempre una literatura oral que nadie llegó a escribir.",
        fuente = "Catálogos de lenguas del mundo; UNESCO, lenguas en peligro",
        preguntas = listOf(
            Pregunta("¿Cuántas lenguas se hablan aproximadamente?",
                listOf("Unas siete mil", "Unas setecientas",
                    "Unas setenta mil", "Unas mil quinientas")),
            Pregunta("¿De qué depende la cifra exacta?",
                listOf("De dónde pongas la frontera entre lengua y dialecto",
                    "Del número de países", "De cuántas están escritas",
                    "De los censos de cada década")),
            Pregunta("¿Cuánto podría desaparecer este siglo?",
                listOf("Entre un tercio y la mitad", "Menos del uno por ciento",
                    "Casi todas", "Unas cincuenta"))
        )
    ),

    Capsula(
        id = "esperanto",
        categoria = TemaReto.LENGUAJE,
        gancho = "¿Sabes quién se inventó una lengua para que nadie se peleara?",
        texto = "Un oftalmólogo. Ludwik Zamenhof creció en Białystok, una ciudad donde " +
            "convivían polacos, rusos, alemanes y judíos, cada grupo en su idioma, y " +
            "atribuía buena parte de las peleas a que no se entendían. En 1887 publicó " +
            "una gramática de una lengua nueva, diseñada para aprenderse rápido: " +
            "dieciséis reglas, ninguna excepción, verbos totalmente regulares y " +
            "vocabulario tomado de las lenguas europeas más extendidas. Firmó el " +
            "folleto con el seudónimo Doktoro Esperanto, el doctor que espera, y el " +
            "seudónimo acabó dando nombre a la lengua. Hoy la hablan del orden de un " +
            "par de millones de personas, y hay incluso un puñado de personas que la " +
            "tienen como lengua materna.",
        fuente = "Ludwik Zamenhof, «Unua Libro» (1887)",
        preguntas = listOf(
            Pregunta("¿A qué se dedicaba Zamenhof?",
                listOf("Era oftalmólogo", "Era maestro", "Era abogado", "Era tipógrafo")),
            Pregunta("¿Cuántas reglas tiene la gramática original?",
                listOf("Dieciséis", "Sesenta", "Cuatro", "Cien")),
            Pregunta("¿Qué significa el seudónimo con el que firmó?",
                listOf("El doctor que espera", "El doctor de la paz",
                    "El doctor errante", "El doctor sin patria"))
        )
    ),

    Capsula(
        id = "alfabetos",
        categoria = TemaReto.LENGUAJE,
        gancho = "¿Sabes por qué escribimos de izquierda a derecha?",
        texto = "No siempre fue así, ni siquiera en el mismo idioma. Los griegos " +
            "arcaicos usaban un sistema llamado bustrofedón, palabra que significa " +
            "«como ara el buey»: escribían una línea hacia la derecha, la siguiente " +
            "hacia la izquierda, la siguiente otra vez a la derecha, y así, girando las " +
            "letras como en un espejo en las líneas invertidas. Se leía sin levantar la " +
            "vista, igual que el arado no vuelve en vacío al final del surco. Con el " +
            "tiempo se impuso una sola dirección, y en el caso griego fue la de " +
            "izquierda a derecha, que pasó al latín y de ahí a nosotros. Las lenguas " +
            "semíticas, que venían de la misma familia de alfabetos, se quedaron con la " +
            "contraria.",
        fuente = "Epigrafía griega arcaica: inscripciones en bustrofedón",
        preguntas = listOf(
            Pregunta("¿Qué significa «bustrofedón»?",
                listOf("Como ara el buey", "Escritura del templo",
                    "Doble sentido", "Letra de piedra")),
            Pregunta("¿Qué pasaba con las letras en las líneas invertidas?",
                listOf("Se giraban como en un espejo", "Se escribían más grandes",
                    "Se separaban con puntos", "Se omitían las vocales")),
            Pregunta("¿Con qué se compara la forma de leerlo?",
                listOf("Con el arado, que no vuelve en vacío",
                    "Con el vuelo de un pájaro", "Con el tejido de una red",
                    "Con el paso de un río"))
        )
    )
)

object Reto {

    /** Cuantas preguntas seguidas hay que acertar. Fallar una empieza de cero. */
    const val PREGUNTAS_POR_CAPSULA = 3

    /** Las categorias que se le ofrecen al usuario para elegir. */
    fun categoriasAlAzar(cuantas: Int = 3): List<TemaReto> =
        TemaReto.entries.shuffled().take(cuantas)

    /**
     * Una capsula de esa categoria que el usuario no haya leido todavia.
     *
     * Sin llevar la cuenta de lo visto, tarde o temprano te toca dos veces la
     * misma y la segunda ya no hay que leer nada. Cuando se agotan las de una
     * categoria se vuelve a empezar con ella, pero nunca repitiendo la de justo
     * antes.
     */
    fun siguiente(categoria: TemaReto, vistas: Set<String>, evitar: String?): Capsula {
        val todas = CAPSULAS.filter { it.categoria == categoria }
        val frescas = todas.filter { it.id !in vistas && it.id != evitar }
        val pozo = frescas.ifEmpty { todas.filter { it.id != evitar }.ifEmpty { todas } }
        return pozo[Random.nextInt(pozo.size)]
    }

    /** Cuantas capsulas quedan por leer en una categoria, para enseñarlo al elegir. */
    fun frescasEn(categoria: TemaReto, vistas: Set<String>): Int =
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
