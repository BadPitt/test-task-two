### Решение задачи 2
<b>Задача 2. Группировка данных</b> \
Напишите программу, которая считает статистику по операциям.
Данные об операциях находятся в файле, который сгенерирован в предыдущей задаче.
Программа должна подсчитать сумму всех операций за каждый день и суммы всех операций в каждой точке продаж.
Программе в качестве параметров передаются имя файла с операциями, имя файла со статистикой по датам, имя файла со статистикой по точкам продаж.
Статистика по датам должна быть отсортирована по возрастанию дат.
Статистика по точкам продаж должна быть отсортирована по убыванию суммы. \
Пример запуска программы: \
```java -jar build/libs/test-task-two-1.0.0.jar src/test/resources/input.txt  src/test/resources/result-by-date.txt src/test/resources/result-by-office.txt```

Подпроект test содержит бенчмарки для измерения времени, необходимого для рассчета показателей по офисам и датам.

Ниже приведены результаты запуска на машине с характеристиками: \
JVM - version: JDK 1.8.0_172, Java HotSpot(TM) 64-Bit Server VM, 25.172-b11 \
OS  - Ubuntu 17.10 \
CPU - Intel® Core™ i3-7100 CPU @ 3.90GHz × 4 \
RAM - 15,6 GiB \
HDD: 

```$sudo hdparm -Tt /dev/sda  ``` \
/dev/sda: \
 Timing cached reads:   12866 MB in  2.00 seconds = 6443.46 MB/sec \
 Timing buffered disk reads: 182 MB in  3.02 seconds =  60.24 MB/sec \
```$dd bs=1M count=256 if=/dev/zero of=test conv=fdatasync ``` \
256+0 records in \
256+0 records out \
268435456 bytes (268 MB, 256 MiB) copied, 3,56009 s, 75,4 MB/s

т.е. скорость записи минуя кэши: ~75,4 MB/s



| Benchmark                                | Mode | Cnt | Score  | Error | Units |
| ---------------------------------------- |:----:|:---:|:------:|:-----:| -----:|
| MyBenchmark.testCalculateByFiles_1000    | avgt |  25 | 0.066 ±|  0.006|  s/op |
| MyBenchmark.testCalculateByFiles_10_000  | avgt |  25 | 2.945 ±|  0.444|  s/op |
| MyBenchmark.testCalculateByFiles_100_000 | avgt |  25 |12.103 ±|  3.014|  s/op |
| MyBenchmark.testCalculate_1000           | avgt |  25 | 0.006 ±|  0.001|  s/op |
| MyBenchmark.testCalculate_10_000         | avgt |  25 | 0.021 ±|  0.002|  s/op |
| MyBenchmark.testCalculate_100_000        | avgt |  25 | 0.253 ±|  0.044|  s/op |

testCalculateByFiles_* используют меньшее кол-во хипа(~300Mb), но проигрывают по времени. \
testCalculate_* используют большее кол-во хипа(~800Mb), но выигрывают по времени. \
Утилизация CPU практически одинаковая.