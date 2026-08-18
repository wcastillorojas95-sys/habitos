package com.lucas.habitos

import kotlin.random.Random

/**
 * El reto que hay que superar para abandonar una actividad antes de tiempo.
 *
 * Antes bastaba con mantener pulsado tres segundos, o escribir un PIN que uno
 * mismo se habia puesto. Las dos cosas se convierten en gesto reflejo a la
 * tercera vez: la mano aprende el atajo y la cabeza no llega a intervenir.
 *
 * Una fabula con una pregunta de comprension no se puede hacer en automatico.
 * Hay que leer de verdad, y leer tarda. Para cuando contestas ya han pasado
 * dos minutos pensando en otra cosa, que es justo el hueco que necesitaba el
 * impulso de rendirse para pasarse solo.
 *
 * Las fabulas son versiones propias de historias de dominio publico: Zhuangzi,
 * Esopo, los estoicos, los cuentos de Nasrudin, koanes zen. Van dentro de la
 * app, sin internet de por medio.
 */
data class Fabula(
    val titulo: String,
    val texto: String,
    val pregunta: String,
    val opciones: List<String>,
    /** Indice de la opcion correcta dentro de [opciones], antes de barajarlas. */
    val correcta: Int
)

/**
 * Las preguntas van sobre un detalle concreto del texto, nunca sobre la moraleja.
 *
 * Es deliberado: una pregunta del tipo "que nos ensena esta fabula" se acierta
 * por sentido comun sin haber leido una linea. Preguntar quien dijo que, o que
 * hizo el protagonista exactamente, solo se contesta leyendo.
 */
val FABULAS: List<Fabula> = listOf(

    Fabula(
        titulo = "La mariposa de Zhuangzi",
        texto = "Zhuangzi sonó una noche que era una mariposa. Volaba de flor en " +
            "flor, contenta, sin acordarse en ningún momento de que existiera un " +
            "hombre llamado Zhuangzi. Al despertar se encontró tumbado en su " +
            "estera, con su cuerpo de siempre y sus preocupaciones de siempre. " +
            "Pero se quedó sentado un largo rato sin moverse, porque ya no sabía " +
            "si era un hombre que acababa de soñar que era una mariposa, o si era " +
            "una mariposa que en ese preciso instante estaba soñando que era un " +
            "hombre. Nunca resolvió cuál de las dos cosas era.",
        pregunta = "¿Qué hizo Zhuangzi nada más despertar?",
        opciones = listOf(
            "Se quedó sentado sin moverse un largo rato",
            "Salió a buscar mariposas al jardín",
            "Escribió el sueño antes de olvidarlo",
            "Se lo contó enseguida a sus discípulos"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "El árbol que no servía",
        texto = "Un carpintero cruzaba el bosque con sus aprendices cuando pasaron " +
            "junto a un roble enorme, tan ancho que hacían falta veinte hombres " +
            "para rodearlo. Los aprendices se detuvieron a mirarlo, pero el " +
            "carpintero siguió andando sin levantar la vista. «Esa madera no vale " +
            "nada», les dijo. «Se pudre, se agrieta, no aguanta un barco ni un " +
            "ataúd.» Aquella noche el roble se le apareció en sueños y le habló: " +
            "«Llevas toda la vida buscando árboles útiles, y por eso los talan " +
            "jóvenes. Yo he llegado a viejo y a enorme precisamente porque no " +
            "sirvo para nada. Mi inutilidad es lo que me ha salvado.»",
        pregunta = "Según el árbol, ¿por qué había llegado a ser tan grande?",
        opciones = listOf(
            "Porque no servía para nada",
            "Porque crecía junto a un río",
            "Porque el bosque estaba protegido",
            "Porque los aprendices lo cuidaban"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "La escudilla de Diógenes",
        texto = "Diógenes vivía en un tonel y no tenía más posesiones que un manto, " +
            "un bastón y una escudilla de madera para beber. Presumía de que nadie " +
            "en Atenas necesitaba tan poco como él. Un día, camino de la fuente, " +
            "vio a un niño que se agachaba en la orilla y bebía agua haciendo un " +
            "cuenco con las manos. Diógenes se quedó mirándolo, sacó su escudilla " +
            "y la tiró contra las piedras. «Este crío me ha ganado», dijo. «Llevaba " +
            "años cargando con algo que me sobraba.»",
        pregunta = "¿Qué hizo Diógenes después de ver al niño?",
        opciones = listOf(
            "Tiró su escudilla contra las piedras",
            "Le regaló la escudilla al niño",
            "Se fue a vivir junto a la fuente",
            "Le enseñó al niño a tallar madera"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "Quién sabe si es bueno o malo",
        texto = "A un granjero se le escapó el caballo. Los vecinos vinieron a " +
            "compadecerle y él solo dijo: «Quién sabe si es bueno o malo». A la " +
            "semana el caballo volvió trayendo consigo tres caballos salvajes. Los " +
            "vecinos le felicitaron y él repitió: «Quién sabe si es bueno o malo». " +
            "Su hijo intentó domar uno de los salvajes, se cayó y se rompió la " +
            "pierna. Otra vez las condolencias, otra vez la misma respuesta. Al mes " +
            "llegaron los reclutadores del ejército y se llevaron a todos los " +
            "jóvenes del pueblo. Al hijo del granjero lo dejaron, porque con la " +
            "pierna rota no servía para la guerra.",
        pregunta = "¿Cuántos caballos salvajes trajo el caballo al volver?",
        opciones = listOf("Tres", "Uno", "Siete", "Ninguno, volvió solo"),
        correcta = 0
    ),

    Fabula(
        titulo = "La lámpara de Epicteto",
        texto = "Epicteto tenía una lámpara de hierro junto a la puerta de su casa, " +
            "y una noche entró un ladrón y se la llevó. Al día siguiente sus " +
            "alumnos lo encontraron tranquilo, colocando en el mismo sitio una " +
            "lámpara nueva, esta vez de barro. Le preguntaron cómo no estaba " +
            "furioso. «Mañana volverá», contestó, «y solo encontrará barro. Yo he " +
            "perdido una lámpara de hierro; él ha pagado por ella un precio mucho " +
            "más alto, porque se ha convertido en ladrón. Ese trato no lo haría yo " +
            "ni por cien lámparas.»",
        pregunta = "¿De qué material era la lámpara nueva?",
        opciones = listOf("De barro", "De hierro, igual que la robada", "De bronce", "De piedra"),
        correcta = 0
    ),

    Fabula(
        titulo = "Nasrudín busca la llave",
        texto = "Un vecino encontró a Nasrudín a cuatro patas bajo la farola de la " +
            "plaza, palpando el suelo. Le preguntó qué buscaba y Nasrudín contestó " +
            "que la llave de su casa. El vecino se puso a ayudarle, y estuvieron un " +
            "buen rato rastreando el empedrado sin encontrar nada. Al final el " +
            "vecino le preguntó dónde se le había caído exactamente. «Dentro de " +
            "casa», dijo Nasrudín. El vecino se levantó indignado: «¿Y entonces por " +
            "qué demonios la buscamos aquí?». «Hombre», dijo Nasrudín, «porque aquí " +
            "hay luz. En casa está todo oscuro.»",
        pregunta = "¿Dónde se le había caído la llave a Nasrudín?",
        opciones = listOf(
            "Dentro de su casa",
            "En la plaza, junto a la farola",
            "En el camino de vuelta del mercado",
            "No lo recordaba"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "La taza que rebosa",
        texto = "Un profesor de universidad fue a visitar al maestro Nan-in para que " +
            "le explicara el zen. Nan-in lo sentó y le sirvió té. Empezó a llenarle " +
            "la taza mientras el profesor hablaba de sus lecturas, de sus teorías y " +
            "de todo lo que ya sabía del asunto. La taza se llenó, y Nan-in siguió " +
            "vertiendo. El té se desbordó, corrió por la mesa y empezó a caer al " +
            "suelo. El profesor por fin se calló y dio un salto: «¡Está rebosando! " +
            "¡Ya no cabe más!». «Como esta taza», dijo Nan-in dejando la tetera, " +
            "«vienes lleno. ¿Cómo voy a enseñarte nada si no vacías primero?»",
        pregunta = "¿Qué hizo Nan-in mientras el profesor hablaba?",
        opciones = listOf(
            "Seguir sirviendo té hasta que la taza se desbordó",
            "Cerrar los ojos y quedarse en silencio",
            "Tomar notas de lo que decía",
            "Salir de la sala sin dar explicaciones"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "La segunda flecha",
        texto = "Un maestro preguntó a sus discípulos qué siente una persona a la que " +
            "alcanza una flecha. «Dolor», contestaron. «¿Y si a esa misma persona la " +
            "alcanza una segunda flecha justo en el mismo sitio?» «Mucho más dolor.» " +
            "El maestro asintió. «En la vida no siempre podéis evitar la primera " +
            "flecha: la enfermedad, la pérdida, el fracaso. Esa llega de fuera y no " +
            "depende de vosotros. Pero la segunda flecha, la de darle vueltas, la de " +
            "reprocharos haberla recibido, la de imaginar cuántas vendrán después, " +
            "esa la disparáis vosotros. Y casi siempre duele más que la primera.»",
        pregunta = "Según el maestro, ¿quién dispara la segunda flecha?",
        opciones = listOf(
            "Uno mismo",
            "El destino",
            "Quien disparó la primera",
            "Nadie: la segunda flecha nunca llega"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "El asno entre dos montones",
        texto = "Contaban los escolásticos el caso de un asno hambriento al que " +
            "pusieron delante dos montones de heno, uno a la izquierda y otro a la " +
            "derecha. Los dos montones eran idénticos: el mismo tamaño, la misma " +
            "hierba, la misma distancia exacta. El asno miró el de la izquierda y " +
            "luego el de la derecha, y no encontró una sola razón para preferir uno " +
            "sobre el otro. Volvió a mirar. Y volvió a mirar. Siguió allí plantado, " +
            "sopesando, hasta que se murió de hambre entre los dos montones intactos.",
        pregunta = "¿Cómo acabó el asno?",
        opciones = listOf(
            "Murió de hambre entre los dos montones",
            "Se comió primero el de la izquierda",
            "Los mezcló en un solo montón",
            "Lo espantó un perro y salió corriendo"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "La barca vacía",
        texto = "Un hombre cruzaba el río en su barca cuando otra barca chocó contra " +
            "la suya de costado. Se levantó furioso y empezó a gritarle al " +
            "responsable que mirara por dónde iba, que era un inútil, que le había " +
            "estropeado la mañana. Entonces vio que la otra barca estaba vacía: se " +
            "había soltado de su amarre y bajaba a la deriva. Los gritos se le " +
            "apagaron en la garganta y siguió remando. El golpe había sido el " +
            "mismo. El río era el mismo. Lo único que había desaparecido era " +
            "alguien a quien culpar.",
        pregunta = "¿Por qué dejó de gritar el hombre?",
        opciones = listOf(
            "Porque vio que la otra barca venía vacía",
            "Porque el otro barquero se disculpó",
            "Porque su barca empezó a hundirse",
            "Porque se dio cuenta de que era su vecino"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "El anillo de Giges",
        texto = "Giges era un pastor al servicio del rey de Lidia. Un terremoto abrió " +
            "una grieta en la tierra y allí abajo encontró un caballo de bronce " +
            "hueco y, dentro, un cadáver con un anillo de oro en el dedo. Se lo " +
            "quedó. Al poco descubrió que si giraba el engaste hacia dentro de la " +
            "mano se volvía invisible, y que si lo giraba hacia fuera reaparecía. En " +
            "cuanto estuvo seguro de lo que tenía, se hizo enviar a palacio, sedujo " +
            "a la reina, mató al rey con su ayuda y ocupó el trono. Glaucón lo " +
            "contaba para preguntar algo incómodo: si a ti te dieran ese anillo, " +
            "¿seguirías siendo justo?",
        pregunta = "¿Dónde encontró Giges el anillo?",
        opciones = listOf(
            "En el dedo de un cadáver, dentro de un caballo de bronce",
            "En el fondo de un pozo del palacio",
            "Se lo regaló el rey de Lidia",
            "Lo compró a un mercader extranjero"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "La piedra en el camino",
        texto = "Un rey mandó poner una piedra enorme en mitad del camino principal y " +
            "se escondió a mirar. Pasó un comerciante rico y la rodeó quejándose del " +
            "abandono de los caminos. Pasaron soldados, cortesanos y campesinos, y " +
            "todos la rodearon, y muchos culparon al rey por no mantener el reino en " +
            "condiciones. Al atardecer pasó un campesino cargado de verdura. Dejó el " +
            "fardo en el suelo, empujó la piedra un buen rato hasta apartarla y, al " +
            "levantarla, encontró debajo una bolsa con monedas de oro y una nota del " +
            "rey: para quien aparte la piedra del camino.",
        pregunta = "¿Qué había debajo de la piedra?",
        opciones = listOf(
            "Una bolsa de monedas de oro y una nota del rey",
            "Un pozo tapado hacía años",
            "Nada en absoluto",
            "Las huellas de un carro real"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "Los tres filtros",
        texto = "Un conocido abordó a Sócrates en la calle para contarle algo urgente " +
            "sobre un amigo suyo. Sócrates lo paró antes de que empezara. «Déjame " +
            "hacerte tres preguntas. La primera: ¿estás completamente seguro de que " +
            "lo que vas a contarme es verdad?» El hombre admitió que lo había oído " +
            "por ahí. «La segunda: ¿es algo bueno?» No lo era, más bien lo " +
            "contrario. «La tercera: ¿me va a servir de algo saberlo?» El hombre " +
            "reconoció que probablemente no. «Entonces», dijo Sócrates, «si no es " +
            "verdad, ni es bueno, ni me sirve, ¿para qué contármelo?»",
        pregunta = "¿Cuál fue la primera pregunta de Sócrates?",
        opciones = listOf(
            "Si estaba seguro de que era verdad",
            "Si era algo bueno",
            "Si le iba a servir de algo",
            "Quién se lo había contado"
        ),
        correcta = 0
    ),

    Fabula(
        titulo = "El elefante a oscuras",
        texto = "Trajeron un elefante a una ciudad donde nadie había visto uno, y lo " +
            "dejaron en una sala sin ventanas. La gente entraba a oscuras y solo " +
            "podía palparlo. El que le tocó la trompa salió diciendo que un elefante " +
            "es como una serpiente gruesa. El que le tocó la oreja juró que es como " +
            "un abanico. El que dio con la pata insistió en que es como una columna, " +
            "y el que alcanzó el lomo, en que es como un trono. Discutieron hasta la " +
            "noche. Ninguno mentía, y ninguno tenía razón: a cada uno le había " +
            "tocado una parte y la había confundido con el todo.",
        pregunta = "¿Con qué comparó el elefante quien le tocó la oreja?",
        opciones = listOf(
            "Con un abanico",
            "Con una serpiente gruesa",
            "Con una columna",
            "Con un trono"
        ),
        correcta = 0
    )
)

object Reto {

    /**
     * Una fabula al azar, evitando la ultima que salio.
     *
     * Sin esa condicion, tarde o temprano te toca dos veces seguidas la misma y
     * la segunda vez ya no hay que leer nada: el reto se cae solo.
     */
    fun siguiente(evitarTitulo: String?): Fabula {
        val posibles = FABULAS.filter { it.titulo != evitarTitulo }.ifEmpty { FABULAS }
        return posibles[Random.nextInt(posibles.size)]
    }

    /**
     * Segundos que el boton de continuar permanece bloqueado.
     *
     * Calculado sobre las palabras del texto a un ritmo de lectura tranquilo.
     * No es por castigar: es que sin este tope basta con dar a continuar y jugar
     * a adivinar entre cuatro opciones, y entonces esto se convierte en un dado.
     */
    fun segundosDeLectura(fabula: Fabula): Int {
        val palabras = fabula.texto.split(' ').count { it.isNotBlank() }
        return (palabras / 3).coerceIn(20, 45)
    }
}
