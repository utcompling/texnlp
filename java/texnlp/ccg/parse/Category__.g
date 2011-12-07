lexer grammar Category;
@header {
package austinnlp.ccg.parse;
}

T8 : '[' ;
T9 : ']' ;
T10 : '(' ;
T11 : ')' ;

// $ANTLR src "Category.g" 104
BASESTRING: ( UPPER | LOWER | '.' | ',' | ':' | ';')+ ;
// $ANTLR src "Category.g" 105
fragment UPPER: 'A'..'Z';
// $ANTLR src "Category.g" 106
fragment LOWER: 'a'..'z';
// $ANTLR src "Category.g" 107
SLASH: '\\' | '/' ;
