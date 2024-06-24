# NTHU_109062274_楊子慶 ACAL 2024 Spring Lab 4 Homework Submission

###### tags: `AIAS Spring 2024`

## How to run test

I write a script to help us run tasks so easily, located at `lab04/scripts/run`. One can open lab04 working directory and:

* Run test
    ```bash
    # ./scripts/run check <PKG/TEST> # run test(s) in PKG or just a TEST
    # ./scripts/run test <PKG/TEST> # run test(s) and generate corresponding VCD simulations in PKG or just a TEST
    # e.g.
    ./scripts/run check Lab # run tests in Lab package
    ./scripts/run test Hw1.MixAdderTest # run single test class MixAdderTest and generate corresponding VCD simulations
    ./scripts/run test Hw2 # run tests and generate corresponding VCD simulations in Hw2 package
    ```
* Clean compiled targets
    ```bash
    ./scripts/run clean gen # remove generated directory
    ./scripts/run clean tar # remove compiled .class target
    ./scripts/run clean test # remove compiled test-runner directory
    ./scripts/run clean all # remove all compiled resources
    ```

## Hw4-1 Mix Adder

### Scala Code

#### Coherence of coding style

If we take a look at the implementation of adders in `Lab4-X`, we'll see that the coding style of bundle IO members are **NOT coherence**: some ports are capitailzed in a module whereas camelcased in another module even the port name are the same, which is like scratching black board with nails, so painful.

In this homework, we gonna [follow the naming convention that chisel3 (Scala) favors](https://www.chisel-lang.org/docs/developers/style#naming-conventions):

* For module/class: Capitalized
* For variable/value: camelCase

:::danger
Without doubt, I apply this change onto all the testbench (Module IO) reasonably.

For determined (template) module name and package name, I remain them not touched... even though most of them are sadly mismatch the [convention of official style](https://www.chisel-lang.org/docs/developers/style#packages).
:::

Nice, let's start programming!

#### Factory patterns

Before we implement `MixAdder`, one can utilize the OOP factory patterns introduced by Scala (Ref: chapter 14 of [chisel tutorial](https://inst.eecs.berkeley.edu/~cs250/sp17/handouts/chisel-tutorial.pdf)) to elegantly utilizing `CLAdder` module implemented in `Lab`.

```scala=
/**
 * Singleton to make CLAdder (CLAdder factory)
 */
object CLAdder {
  def apply() = {
    Module(new CLAdder())
  }

  /**
   * Make a CLAdder from IO ports
   *
   * This method will wrap CLAdder with `Module`
   * undoubtedly in order to assign the ports
   */
  def apply(cin: UInt,
            in1: UInt,
            in2: UInt,
            sum: UInt,
            cout: UInt) = {

    /**
     * Always wrap instantiated module with `Module`
     * if we're going to manipulate it to assure
     * chisel trace IO decently.
     */
    val cla = Module(new CLAdder())

    /**
     * Connect io ports p.s. `TO_BITS := FROM_BITS`
     */

    // inputs: from arguments to cla inputs
    cla.io.cin := cin
    cla.io.in1 := in1
    cla.io.in2 := in2
    // outputs: from cla outputs to arguments
    sum := cla.io.sum
    cout := cla.io.cout
    // return result
    cla
  }
}
```

Samely, one can apply the same thing onto given `MixAdder` template and other adders.

```scala=
/**
 * Singleton to make MixAdder
 */
object MixAdder {
  def apply(n: Int) = {
    Module(new MixAdder(n))
  }
}
```

Now we're well-prepared to implement `MixAdder`.

#### MixAdder

```scala=
class MixAdder(n: Int) extends Module {

  val batchSize = 4

  /**
   * Get width of `CLAdder`, i.e. `batchSize * n`
   */
  def batch(n: Int) = batchSize * n

  val io = IO(new Bundle {
    val cin = Input(UInt(1.W))
    val in1 = Input(UInt(batch(n).W))
    val in2 = Input(UInt(batch(n).W))
    val sum = Output(UInt(batch(n).W))
    val cout = Output(UInt(1.W))
  })

  val carry = Wire(Vec(n + 1, UInt(1.W)))
  val sum = Wire(Vec(n, UInt(batchSize.W)))

  carry(0) := io.cin

  for (i <- 0 until n) {

    /**
     * Use `UInt.apply(high, low)` to slice a Uint
     * works **inclusively**.
     *
     * Ref: source code at `chisel3/Bits.scala`
     */
    CLAdder(
      cin = carry(i),
      in1 = io.in1(batch(i + 1) - 1, batch(i)),
      in2 = io.in2(batch(i + 1) - 1, batch(i)),
      sum = sum(i),
      cout = carry(i + 1))
  }

  io.sum := sum.asUInt
  io.cout := carry(n)
}
```

In CLAdder answer template, although we can see that one use an array to store the io of adder modules in order to wire them IOs, one can actually take advantage of factory singleton `CLAdder` I mentioned in the last section to write more elegant and simple code with also better performance for Scala code to run. That is, utilizing the factory method: `CLAdder::apply (2 overrides)` in for loop.

### Test Result

One can test with...

```bash
./scripts/run test Hw1
```

Text below is the partial result of running `MixAdderTest`.

* Console
    ```log00
    [info] [0.001] SEED 1711466863917
    [info] [0.052] MixAdder test completed!!!!!
    Enabling waves..
    Exit Code: 0
    [info] [0.057] RAN 1000 CYCLES PASSED
    ```
* VCD
    ![](https://course.playlab.tw/md/uploads/7cc66798-33ad-4723-9be3-4e84c0e95f56.png)

## Hw4-2 Add-Suber
### Scala Code

For those who has taken the Digital design course, this lab (in Verilog version) is perhaps the very first lab that one may meet. I'm also the one of them. At that time, I drew the logic diagram to show the design of it:

![](https://course.playlab.tw/md/uploads/51f05dcc-08c5-45b2-b792-f886a2e02c68.png)

with verilog code implemented as:

```systemverilog=
`define n 4

module SignedAddSub(a, b, m, v, s);
    input [`n-1:0] a, b;
    input m;
    output v;
    output [`n-1:0] s;

wire [`n-1:0] nb; // negate b array
wire [`n:0] cry; // carrys

assign cry[0] = m;
assign nb = b ^ { `n {m} }; // { 4 { m } } == { m, m, m, m }

genvar i;
generate
    for (i = 0; i < `n; i = i + 1) begin : gen_loop
        fullAdder fa(
            .a(a[i]), .b(nb[i]), .c_in(cry[i]),
            .s_bit(s[i]), .c_out(cry[i+1])
        );
    end
endgenerate

assign v = cry[`n-1] ^ cry[`n];

endmodule
```

We can just translate it into Chisel and finish this homework as:

```scala=
/**
 * Generalized twos complement add-subtractor
 */
class Add_Suber(n: Int = 4) extends Module {
  val io = IO(new Bundle {
    val in_1 = Input(UInt(n.W))
    val in_2 = Input(UInt(n.W))
    val op = Input(Bool()) // 0: ADD 1: SUB
    val out = Output(UInt(n.W))
    val o_f = Output(Bool())
  })

  val carries = Wire(Vec(n + 1, UInt(1.W)))
  val sums = Wire(Vec(n, UInt(1.W)))

  carries(0) := io.op

  for (i <- 0 until n) {
    FullAdder(cin = carries(i),
              in1 = io.in_1(i),
              in2 = io.in_2(i) ^ io.op,
              sum = sums(i),
              cout = carries(i + 1))
  }

  io.out := sums.asUInt()
  io.o_f := carries(n - 1) ^ carries(n)
}
```

:::success
Notice that I make use of class primary constructor with default value to generate size-customizable module, just like what I written with Macros in verilog. Default value let me need not to modify testbench but can still implement a flexible module.
:::

### Test Result

One can test with...

```bash
./scripts/run test Hw2
```

Text below is partial result of running `Add_SuberTest`.

* Console
    ```log
    [info] [0.001] SEED 1711496119373
    [info] [0.026] Add_Suber test completed!!!
    Enabling waves..
    Exit Code: 0
    [info] [0.029] RAN 512 CYCLES PASSED
    ```
* VCD
    ![](https://course.playlab.tw/md/uploads/b1545bfb-a42d-4ea2-891d-424ca7a31189.png)


## Hw4-3 Booth multiplication

### Design

Given that I'm still very new to using Chisel as  hardware design platform, I dare not implement the whole logic of Booth multiplication in only one shot. Rather, I should design and implement and test it step-by-step, helping me to get used to this environment and enhance the experience of writting testbench.

Before implementing it, let us do a little analysis on the design of Booth Multipler...

#### Why Booth multiplication

The key to why Booth Multipler can reduce numbers of partial-multiplication and addition runs is that we **re-encode multipler** from binary into some asymmetric encoding (trinary: `[-1, 0, 1]`, fifnary: `[-2, -1, 0, 1, 2]`, etc) to shrink the length of multipler. In other words, we'll have less partial runs whereas doing more things in each calculation of partial result.

Speaking of the choice of asymmetric encoding, we may favor the fifnary one since:

* Trinary actually doesn't save numbers of runs
* Fifnary saves a huge numbers of runs, also it's so easy to generate partial result, which is because that $\times 2$ or $\times -2$ means to shift multiplcand left by one in binary representation of multiplcand.
* For sevenary, ninary, etc, in order to calculate partial result, we'll need more complicated multipliers to calculate it, with. saving fewer partial runs, which is not a good choice.

#### How we design

The algorithm is like:

1. Encode multiplier to `[-2, -1, 0, 1, 2]` so the length of multipler is shrinked to half of the origin one.
2. Do multiplication according to each encoded multiplier.
3. Sum up all the result.

With respect to the second part, I design `PartialBooth` module that encodes multiplier on demand according to given sliding window, and then calculates partial result of multiplication for us.

```scala=
object PartialBooth {
  def apply(width: Int)(in1: UInt,
                        in2Window: UInt,
                        partialOut: SInt) = {
    val pb = Module(new PartialBooth(width))
    pb.io.in1 := in1
    pb.io.in2Window := in2Window
    partialOut := pb.io.partialOut

    pb
  }
}
```

Within `PartialBooth`, I further design a `MultiplyWithinTwo` module that actually deals with multiplication of **binary-multiplicand** and **single-digit-fifnary-multipler**.

```scala=
object MultiplyWithinTwo {
  def apply(width: Int)(num: UInt,
                        by: UInt,
                        res: SInt) = {
    val m = Module(new MultiplyWithinTwo(width))
    m.io.num := num
    m.io.by := by
    res := m.io.res

    m
  }
}
```

### Implementation and test

#### `MultiplyWithinTwo`

Notice that we define **output** as **Signed Integer** so that we don't need to deal with signed extension for the result of the multiplication.

To "select" the negative result for cases that we multiply a binary with $-1$ or $-2$ **with the less number of selector width**, we encode fifnary to binary as what we've documented for this module.

```scala=
/**
 * An multiplication that can multiply a binary with
 * [-2, -1, 0, 1, 2]
 *
 * For multiplier, we define it as:
 * ```scala
 * [ // value: "by" argument
 *   0: 0
 *   1: 1
 *   2: 2
 *   -1: 3
 *   -2: 4
 * ]
 * ```
 */
class MultiplyWithinTwo(width: Int) extends Module {

  val io = IO(new Bundle {
    val num = Input(UInt(width.W))
    val by = Input(UInt(3.W))
    val res = Output(SInt((width + 1).W))
  })

  val num = io.num.asSInt()

  io.res := MuxLookup(io.by,
                      0.S,
                      Seq(
                        0.U -> 0.S,
                        1.U -> num,
                        2.U -> (num << 1),
                        3.U -> -num,
                        4.U -> ((-num) << 1)
                      ))
}
```

After the implementation, one can test whether it's functioning with `MultiplyWithinTwoTest`:

:::info
Note that we use pattern matching expression to determine multiplier, which is a common syntax for modern programming language like `Scala`, `Rust` and `Java SE 14 (up)`...
:::

```scala=
class MultiplyWithinTwoTest(m: MultiplyWithinTwo)
    extends PeekPokeTester(m) {
  for (a <- 0 until 1 << 3) {
    for (b <- 0 until 5) {
      poke(m.io.num, a)
      poke(m.io.by, b)

      var res = a * (b match {
        case 0 => 0
        case 1 => 1
        case 2 => 2
        case 3 => -1
        case 4 => -2
      })

      expect(m.io.res, res)
      step(1)
    }
  }
  println("MultiplyWithinTwo test completed!!!")
}
```

with command:

```bash
./scripts/run check Hw3.MultiplyWithinTwoTest
```

and will see result of

```scala
[info] [0.001] SEED 1711545756064
[info] [0.005] MultiplyWithinTwo test completed!!!
test MultiplyWithinTwo Success: 40 tests passed in 45 cycles in 0.010712 seconds 4200.87 Hz
[info] [0.006] RAN 40 CYCLES PASSED
```

#### `PartialBooth`

With `MultiplyWithinTwo`, the remained mission to implement `PartialBooth` is to make a lookup table that choose correct multipler for `MultiplyWithinTwo`.

```scala=
/**
 * Partial result of Booth Multiplication
 */
class PartialBooth(width: Int) extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(width.W)) // Multiplicand
    val in2Window =
      Input(UInt(width.W)) // Multiplier
    val partialOut =
      Output(SInt((width + 1).W)) // product
  })

  val amp = MuxLookup( // amplitude
    io.in2Window,
    0.U,
    Seq(
      "b000".U -> 0.U,
      "b001".U -> 1.U,
      "b010".U -> 1.U,
      "b011".U -> 2.U,
      "b100".U -> 4.U, // 4 is -2
      "b101".U -> 3.U, // 3 is -1
      "b110".U -> 3.U, // 3 is -1
      "b111".U -> 0.U
    )
  )

  MultiplyWithinTwo(width = width)(num = io.in1,
                                   by = amp,
                                   res =
                                     io.partialOut)
}
```

#### `Booth_Mul`

With `PartialBooth`, one can just utilize a vector of them like ripple carry adder.

```scala=
/**
 * Radix-4 Booth Multiplier
 */
class Booth_Mul(width: Int) extends Module {
  val runs = width / 2
  val outWidth = 2 * width

  val io = IO(new Bundle {
    val in1 = Input(UInt(width.W)) // Multiplicand
    val in2 = Input(UInt(width.W)) // Multiplier
    val out = Output(UInt(outWidth.W)) // product
  })

  // Generate dummy tailing zero for io.in2
  val in2 = io.in2 << 1

  val partials = Wire(
    Vec(runs + 1, SInt(outWidth.W)))

  partials(0) := 0.S

  for (i <- 0 until runs) {
    val window = in2(2 * i + 2, 2 * i)
    val partial = Wire(SInt((width + 1).W))
    val pb =
      PartialBooth(width)(io.in1, window, partial)

    partials(i + 1) := partials(i) +
      (partial << (2 * i))
  }

  io.out := partials(runs).asUInt()
}
```

One trick appears at line 15 that wee can make a dummy tailing zero for first sliding window.

We can test it with:

```bash
./scripts/run test Hw3.Booth_MulTest
# or test all using ./scripts/run test Hw3
```

and the result will be:

```log
[info] [0.000] SEED 1711547908564
[info] [0.006] Booth_Mul test completed!!!!!
Enabling waves..
Exit Code: 0
[info] [0.008] RAN 25 CYCLES PASSED
```
