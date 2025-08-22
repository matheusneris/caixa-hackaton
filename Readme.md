# Minha interpretações dos campos não especificados explicitamente: 
* Eu interpretei o campo "valorTotalParcelas" como sendo a soma de todas as parcelas da simulação.  Como não tinha especificação entre se era do SAC ou PRICE, assumi que o cliente ficaria com a que tivesse o menor valor final pago, assumindo que seria melhor para ele.

* A "taxaMediaJuro" foi calculada de forma ponderada, considerando a taxa e o valor desejado de cada simulação.

* O "valorTotalCredito" foi assumido que seria a soma de todas as parcelas de cada simulação, sempre pegando a que tem o menor valor final (entre SAC e PRICE), pois assumi que o cliente vai preferir a modalidade que ele paga menos no final das contas.

# Notas sobre o banco SQL Server

* Consegui conectar no banco SQL Server e no Postgres simultâneamente, mas tive problemas na hora de fazer operações distintas em dois bancos simultâneos na aplicação (leitura no SQL Server e escrita no Postgres).
Se você está lendo isso significa que tive que entregar o projeto na branch que para resolver essa questão criei a tabela "Produtos" e fiz as inserções de exemplo do pdf no meu banco Postgres, pois não tive tempo hábil o suficiente.
* Essa é a única parte do desafio que ficou pendente.