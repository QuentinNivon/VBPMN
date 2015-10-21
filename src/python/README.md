# Comparison of PIF Processes

There are two scripts:

- `compare.py` to perform comparison based on equivalences or preorders, including support for hiding, exposal, renaming, and context-awareness

- `checkprop.py` to perform comparison based on a temporal logic property

## Equivalence and Preorder

### Syntax

You can type in `python checkprop.py -h` to get information about the use of it.

In short: `python checkprop.py Model1 Model2 operation [options]`

This command converts each PIF file into an LTS (internal behavior
removed), and compares them wrt. the given operation (strong
equivalence/preorder). Options can be used for hiding/renaming
purposes.

There are 3 available operations:

- `conservative`: both PIF models are equivalent

- `inclusive`: the second PIF model simulates the first one

- `exclusive`: the first PIF model simulates the second one

There are different options:

- `--hiding` is used to hide elements of the model alphabets to **hide** before checking equivalence/preorder.
- `--exposemode` can be used (in conjunction with `--hiding`) to give elements of the alphabets to **keep** (*i.e.*, not to hide) instead of those to hide.

### Examples (from FASE'16)

compare.py model1.pif model2.pif conservative

compare.py model1.pif model2.pif inclusive

compare.py model2.pif model1.pif exclusive

compare.py model1.pif model2.pif conservative --hiding log

compare.py model1.pif model2.pif conservative --hiding a b --exposemode

## Comparison with reference to a Temporal Logic Formula

$ python checkprop.py file1.pif file2.pif file.mcl

This command converts each PIF file into an LTS (internal behavior
removed), and checks that both process LTSs respect the property
specified in file.mcl. 

