# learning-chisel3

自分で書いたChiselのコードで後々見返したくなりそうなやつを集めておくリポジトリ。
大体はブログで取り上げてコードを一部掲載したものが多くなるはず。

## 必要なもの

以下の２つが動く環境

- Scala
- sbt - 1.2.7

## 実行の方法

通常のsbt使ったプロジェクトと同様。<br>
基本的にトピック毎にサブプロジェクトを作成していく予定なので、動かしたいプロジェクトに切り替えて実行するのが良い気がする。
なお以下に書いているコマンドは`sbt`を起動した状態で入力する形で記載しているので注意。

## 現在登録されているプロジェクト

- [chiselFlatSpec](#chiselFlatSpec)
- [chiselFlasSpecWithArgs](#chiselFlasSpecWithArgs)
- [treadleOrFirrtlSim](#treadleOrFirrtlSim)
- [xorShift](#xorShift)
- [bundleAlias](#bundleAlias)
- [uintAndSIntShift](#uintAndSIntShift)
- [parallelTestExecution](#parallelTestExecution)
- [loadChiselMem](#loadChiselMem) 
- [memND](#memND) 

### chiselFlatSpec
その名の通りChiselFlatSpecについて調査した際にサンプルとして作成したプロジェクト。
以下で実行可能なはず。

```bash
project chiselFlatSpec
test"
```

### chiselFlasSpecWithArgs
ChiselFlatSpec使った形式のテストを実行する際にプログラム引数を渡す方法が無いかを試したプロジェクト。
Chiselのモジュール自体はChselFlatSpecプロジェクトと一緒で、テストモジュールに引数処理部分を追加したもの。
単純に実行するだけなら、以下で可能。

```bash
project chiselFlatSpecWithArgs
test
```

プログラム引数を使う場合は以下。

```bash
testOnly MyTester -- -D--backend-name=firrtl -D--generate-vcd-output=on -D--is-verbose=true
```

### treadleOrFirrtlSim

ChiselのPeekPokeTesterを使った試験の際に遭遇した挙動の調査を行うためのサブプロジェクト（作りかけ）

```bash
project chiselFlatSpecWithArgs
test
```

プログラム引数を使う場合は以下。

```bash
testOnly MyTester -- -D--backend-name=firrtl -D--generate-vcd-output=on -D--is-verbose=true
```

### xorShift

XorShiftを使った乱数生成回路用のサブプロジェクト。
Chiselのテスト用メモリのI/Fのタイミングをランダムにするために作ったやつ。
　→　普通にVerilogで書いて埋めたほうが楽な気はするがそこは気にしない。
せっかくなので、ベタ書きのXorShift32版を元にリファクタリングしてChiselっぽく書き換えていくことにする。

```bash
project xorShift
test
```

### bundleAlias

`Bundle`を使ってデータを構造化した場合に、`Bundle`配下のデータの名前が長いな、、どうにか出来るよね、これ。
というのを確認するためのサブプロジェクト。

例えば、わざとらしいけど以下のような`Bundle`を考えた時に`io.a.bb.ccc`にアクセスしやすくすることが出来るかを確かめる

```scala
class A(hasOptPort: Boolean = true) extends Bundle {
  val a = new Bundle {
    val bb = new Bundle {
      val ccc = new Bundle {
        val d = Input(Bool())
        val e = Output(Bool())
        val f = Input(UInt(32.W))
        val g = if (hasOptPort) Some(Output(UInt(32.W))) else None
      }
    }
  }
}

class B(hasOptPort: Boolean = true) extends Module {
  val io = IO(new A)

  io.a.bb.ccc.e := io.a.bb.ccc.d       // Bundleで構造化していくと
  if (hasOptPort) {
    io.a.bb.ccc.g.get := io.a.bb.ccc.f // どんどん深くなっていく
  }
}
```

```bash
project bundleAlias
test
```

### uintAndSIntShift

Chiselで算術右シフトのやり方をキャストをした際の挙動を調べたもの。

```bash
project uintAndSIntShift
test
```

### parallelTestExecution

ScalaTestの`ParallelTestExecution`を使ってChiselFlatSpecを継承して作った
テストクラス内のテストを並列実行する処理を確認するためのサブプロジェクト。

```bash
project parallelTestExecution
```

#### 逐次実行する場合のコマンド

```bash
testOnly SequentialTester
```

#### 並列実行する場合のコマンド

```bash
testOnly ParallelTester
```

### loadChiselMem

Chiselのメモリにファイルから読み込んだデータを設定する機能を試した時のサブプロジェクト。

```bash
project loadChiselMem
testOnly MemoryTester 
```

### memND

Chiselの`Mem`を使って2次元のメモリを作る方法を間検討した時に作成したサブプロジェクト。<br>
System Verilogの以下の記述で出来ることをChiselで実現してみた。

```verilog
reg [3:0][7:0] mem[0:1023]
```

#### RTLの生成コマンド

以下のコマンドで"subprj/mem-nd/src/main/scala/Mem2D.scala"内のMem2DクラスのRTLが生成される。<br>

```bash
project memND
runMain ElaborateMem2D
```

生成したRTLは以下のディレクトリに格納される。"WithWrite"と入っているファイルは`MemBase`クラスの`write`を使って書いた場合のRTLとなる。

- rtl/mem2d
  - Mem2D.anno.json
  - Mem2D.fir
  - Mem2D.v
  - Mem2DWithWrite.anno.json
  - Mem2DWithWrite.fir
  - Mem2DWithWrite.v
  
#### テスト実行コマンド
 
以下のコマンドでテストが実行される。 
 
```bash
 testOnly Mem2DTester
```