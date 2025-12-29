
# Outbox pattern

Quando estamos implementando microserviços ao realizar uma operação em que precisamos publicar um evento em uma fila ou notificar
alguém ou algum sistema, o primeiro pensamento é fazer tudo dentro do mesmo serviço. Então depois que a regra de negócio é executada, nos publicamos 
um evento na fila e pronto. Mas se a fila estiver indisponível no momento? E se o sistema for crítico? Nesse caso é irrecuperável e acaba se tornando
um grande problema. Uma solução é utilizar o padrão **outbox**.

O Outbox pattern propõe que ao realizar uma operação seja persistido no banco de dados o evento o evento correspondente, tudo
na mesma transação de forma atômica, paralelo a isso existe um serviço que está lendo a tabela frequentemente e publicando os 
eventos em uma fila, os eventos enviados são marcados para não haver duplicação de eventos notificados. Caso haja indisponilidade
do serviço de mensageria, não há problema, os eventos estão salvos e podem ser enviados à posteriori. Não há perda, apenas atraso em
notificar os interessados.

![](https://miro.medium.com/0*WUdjvJ6zsVqaKo8p.png)

