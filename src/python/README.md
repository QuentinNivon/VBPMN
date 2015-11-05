# Comparison of PIF Processes

## Syntax

You can type in `python vbpmn.py -h` to get information about the use of it.

In short: `python vbpmn.py Model1 Model2 operation [options]`

This command converts each PIF file into an LTS (internal behavior
removed), and compares them wrt. the given operation (equivalences / preorders / model-checking).
Options can be used for hiding/renaming purposes.

There are 5 available operations:

- `conservative`: both PIF models are equivalent

- `inclusive`: the second PIF model simulates the first one

- `exclusive`: the first PIF model simulates the second one

- `property-and`: checks that both PIF models verify some temporal logic formula

- `property-implied`: checks that if the first PIF model verifies some temporal logic formula then the second one does too

There are different options:

- `--hiding` is used to hide elements of the model alphabets to **hide** before checking equivalence/preorder.
- `--exposemode` can be used (in conjunction with `--hiding`) to give elements of the alphabets to **keep** (*i.e.*, not to hide) instead of those to hide.
- `--renaming` is used to rename elements of the model alphabets
- `--renamed` can be used (in conjunction with `--renaming`) to indicate to which model the renaming applies (both by default)

## Examples

```bash
$ python vbpmn.py model1.pif model2.pif conservative

$ python vbpmn.py model1.pif model2.pif inclusive

$ python vbpmn.py model2.pif model1.pif exclusive

$ python vbpmn.py model1.pif model2.pif conservative --hiding log

$ python vbpmn.py model1.pif model2.pif conservative --hiding a b --exposemode

$ python vbpmn.py model1.pif model2.pif conservative --renaming "a:a" "b:b"

$ python vbpmn.py model1.pif model2.pif conservative --renaming "a:b" "b:c" "c:a" --renamed first

$ python vbpmn.py model1.pif model2.pif conservative --renaming "b:a" "c:b" "a:c" --renamed second

$ python vbpmn.py model1.pif model2.pif conservative --renaming "b:c" --renamed all

$ python vbpmn.py model1.pif model2.pif property-and --formula "mu X  . (< true > true and [ not B ] X)"

```

