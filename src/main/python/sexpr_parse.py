###############################################################################
# Super-primitive Python s-expression parser by Elliott Franco Drabek
#
# This might save you the two hours it took me to put it together, test it, and
# (moderately) speed it up.
#
# It only understands lists and atoms.  It understands atoms to be consecutive 
# runs of non-whitespace, non-parenthesis characters.  It converts all-digit
# atoms into ints.
#
# Public domain
#
# No guarantees
#
# Last modified Fri Aug 29 13:57:25 EDT 2003

import sys

###############################################################################

def _gen_tokens(line):
  line_len = len(line)
  left = 0

  while left < line_len:
    c = line[left]

    if c.isspace():
      left += 1
    elif c in '()':
      yield c
      left += 1

    else:
      right = left + 1
      while right < line_len:
        c = line[right]
        if c.isspace() or c in '()':
          break

        right += 1

      token = line[left:right]
      #if token.isdigit():
      #  token = int(token)
      yield token

      left = right


def parse_tree(line):
  stack = []
  for token in _gen_tokens(line):
    if token == '(':
      stack.append([])

    elif token == ')':
      top = stack.pop()
      if len(stack) == 0:
        yield top
      else:
        stack[-1].append(top)

    elif len(stack) == 0:
      yield token
    else:
      stack[-1].append(token)

  assert len(stack) == 0


