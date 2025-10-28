# Robocode01
Este é mega macho rodo de testes e desenvolvimento dos serelepes academicos de ICO.

Epsilon basicamente vem do meu nome, Vitor, _victor_ do latim, significando vitorioso. Como sabia que teriam diversas versões, e honestamente é bem paia usar o nome v4, v5, ou algo assim, aproveitei que as próprias letras gregas tem valor númerico. Do alpha ao ômega. Então, começaria do VitorAlpha. E obviamente não fiz assim por ser muito personalista. Aí a variação de nome "vitorioso" seria Nikáo. Mas a letra incial seria a letra "ni". Nome ruim. Então fui para um próximo, Heitor, Eκτωρ, que começa com a letra Epsilon

----

# 26/10
## 22:15
Primeira versão seguia código padrão do perseguidor.
Versão beta houve a tentativa de adaptar o tiro, onde o firePower seria adequado para a distância, evitando atirar loucamente. Teria o radar lockado em um inimigo ao invés de girar infinitamente pegando inimigos quaisquer. Os problemas foram examidos via IA, mas não compensou. Robô mal se movimentava. Algumas correções foram feitas na versão Gamma, mas nada demais. Versão alpha se mantinha melhor
Dia 25/10 fiquei das 20 até 4 da manhã pensando e adicionei uma espécie de previsão de posição inimiga, na versão alpha, pois, se ele vai superaquecer, ele faz isso pois atira demais. Se ele acertar todos os tiros, os inimigos morrem e ele atira pouco. Um problema a menos. Meter 3x0 no primeiro tempo é essencial. MAs como o fato do robo chegar perto do inimigo e atirar é uma vantagem gigante que tem sobre outros robos, essa previsão de tiro foi colocada apenas para grandes distâncias. Mas a previsão não deu muito certo. Quando o inimigo mudava a velocidade, para por exemplo virar para o lado, no caso de um **Robot**, ou a velocidade oscilava no caso de um **AdvancedRobot**, ele começava a errar todos os tiros dali em diante. Acredito que o problema é a o e.getDistance(); para pegar a velocidade inicial

## 23:10
Pesquisando e lendo código de robos no github de uns caras do Bangladesh até a Dinamarca, parece que é adequado dedicar uma... função? Método? Como chama isso? não sei ``public double EuSeiOndeVoceEsta() {}``, isso aí em Java. Parece ser algo específico da programação orientada a objetos. O que não entendo. Mas sei que preciso fazer isso. Na minha cabeça fazer isso dentro do onScannedRobot tá ok, mas pelo jeito não é.
Meu código está:
```
double t = e.getDistance()/velocidadeBala;
double posicaoFinal = velocidadeInicial * t + 0.5 * aceleracao * Math.pow(t,2);
double futuroX = Xinicial + Math.sin(anguloAbsoluto) * posicaoFinal;
double futuroY = Yinicial + Math.cos(anguloAbsoluto) * posicaoFinal;
```
o ``t``, pelo que parece, assume que o inimigo vai estar parado até o próximo scan. Essa variável precisa ter os ticks do jogo em conta. Confesso que enquanto colocava pensei nos ticks do jogo, mas achei demais, e fiquei com preguiça. Vai que dava certo sem. https://github.com/jd12/RobocodeTargeting Bom link

# 27/10

## 20:23

### firePower
Para internalizar melhor o conteúdo, vou escrevê-lo aqui. Até para facilitar depois o relatório. 

É importante definir a força do tiro, __fire power__, de acordo com a distância. Quanto maior a distância, mais fácil é de errarmos os tiros, por isso, devemos diminuir a força para evitar perda desnecessária de energia. 
```
double firePower = Math.min(400/enemy.getDistance(), 3);
setFire(Math.min(400 / enemy.getDistance(), 3));
```
Aqui ele pega 400 e divide pela distância inimiga em pixels. E compara esse valor com 3, numero máximo permitido, e vê quem é menor, o menor valor é o escolhido. Ou seja, nesse caso, só será escolhido 3, quando a primeira equação dar maior ou igual a 3. Ou seja, se o inimigo estiver a 134 ou menos pixels de distância, atiraremos com força máxima.





Pelo que parece, nesse link, o amigo usa uma linha de código: `public EnemyBot enemy = EnemyBot();` O que dá sempre erro. Por não saber nada de Java, e ao questionar meu colega que sabe de Java e não ser respondido adequadamente, recorri a IA, que me respondeu: **Nos exemplos de tutoriais e bots modelo do Robocode (como “SampleBot/NormalizedShooter” ou “TrackerBot”), o autor geralmente cria uma classe auxiliar chamada EnemyBot para armazenar as informações sobre o inimigo que está sendo rastreado.**, e nisso, é necessário criar um arquivo java separado, na mesma pasta, com o seguinte código:
```
package templateBots;

import robocode.ScannedRobotEvent;

public class EnemyBot {
    private double bearing;
    private double distance;
    private double energy;
    private double heading;
    private double velocity;
    private String name;

    public EnemyBot() {
        reset();
    }

    public void update(ScannedRobotEvent e) {
        name = e.getName();
        bearing = e.getBearing();
        distance = e.getDistance();
        energy = e.getEnergy();
        heading = e.getHeading();
        velocity = e.getVelocity();
    }

    public void reset() {
        name = "";
        bearing = 0.0;
        distance = 0.0;
        energy = 0.0;
        heading = 0.0;
        velocity = 0.0;
    }

    public boolean none() {
        return name.equals("");
    }

    public double getBearing() {
        return bearing;
    }

    public double getDistance() {
        return distance;
    }

    public String getName() {
        return name;
    }
}
```

Para mim faz total sentido. O que é legal. Estou aprendendo :D.

Nesse link para o giuthub, o autor ele fala bastante sobre normalized bearing. Pois o robô, quando o canhão e radar trabalham de forma autônoma entre si, e em relação ao chassi do tanque, há um problema, que ao chegar no angulo por exemplo 180, e o inimigo está no 179, o canhão da uma volta completa para chegar no 179, ao envés de ir para negativo. Ele propõe uma resolução, mas no nosso caso, um advancedrobot não passa por isso quando usa-se os set adequados. No caso seria uma classe robot normal.

Na parte ii desse artigo, ele fala sobre inheritage, o que me fez entender melhor a orientação a objetos no Java, e porque esse jogo meio que precisa obrigatóriamente ser nessa linguagem. Entender o extends, por exemplo. Mas o que ele pediu e o porquê, eu não entendi, apenas segui o que foi pedido. Quero prever tiros, se isso é necessário, faremos.

***Predictive Targting: Using Trigonometry to impress your friends and destroy your enemies*** Achei engraçado esse título, e exatamente o que eu busco. Pena que não sei nada de trigonometria, não ensinaram na escola, e percebi que quase todos sabem. 

Distance = rate * time
Para prever a posição inimiga, precisamos saber quando ela chega lá
```
// calculate firepower based on distance
double firePower = Math.min(500 / enemy.getDistance(), 3);
// calculate speed of bullet
double bulletSpeed = 20 - firePower * 3;
// distance = rate * time, solved for time
long time = (long)(enemy.getDistance() / bulletSpeed);
```


## 00:49
Infelizmente eu trabalho amanhã, e fiquei com muita dor de cabeça pensando em como usar o ADvancedEnemyRobot para conseguir informações do inimigo. Mas tem jeito mais simples e mais clean. É dó definir uma variável double com 0 no começo do código. Dentro do onScanned, definimos valor a ela. Esse valor vai ser armazenado e reutilizado durante todo o código. Mais fácil

## 01:18
Terminei. Vou dormir