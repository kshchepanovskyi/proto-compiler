lexer grammar ProtoLexer;

PACKAGE
    : 'package'
    ;
SYNTAX
    : 'syntax'
    ;
IMPORT
    : 'import'
    ;
PUBLIC
    : 'public'
    ;
OPTION
    : 'option'
    ;
MESSAGE
    : 'message'
    ;
GROUP
    : 'group'
    ;
OPTIONAL
    : 'optional'
    ;
REQUIRED
    : 'required'
    ;
REPEATED
    : 'repeated'
    ;
ONEOF
    : 'oneof'
    ;
EXTEND
    : 'extend'
    ;
EXTENSIONS
    : 'extensions'
    ;
TO
    : 'to'
    ;
MAX
    : 'max'
    ;
RESERVED
    : 'reserved'
    ;
ENUM
    : 'enum'
    ;
SERVICE
    : 'service'
    ;
RPC
    : 'rpc'
    ;
RETURNS
    : 'returns'
    ;
STREAM
    : 'stream'
    ;
MAP
    : 'map'
    ;
BOOLEAN_VALUE
    : 'true'
    | 'false'
    ;
DOUBLE
    : 'double'
    ;
FLOAT
    : 'float'
    ;
INT32
    : 'int32'
    ;
INT64
    : 'int64'
    ;
UINT32
    : 'uint32'
    ;
UINT64
    : 'uint64'
    ;
SINT32
    : 'sint32'
    ;
SINT64
    : 'sint64'
    ;
FIXED32
    : 'fixed32'
    ;
FIXED64
    : 'fixed64'
    ;
SFIXED32
    : 'sfixed32'
    ;
SFIXED64
    : 'sfixed64'
    ;
BOOL
    : 'bool'
    ;
STRING
    : 'string'
    ;
BYTES
    : 'bytes'
    ;
COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;
LINE_COMMENT
    : '//' ~('\r' | '\n')* -> channel(HIDDEN)
    ;
PLUGIN_DEV_MARKER
    // Non-protobuf.
    // Added for convenience of IntelliJ IDEA plugin development
    : ('<caret>'
    | '<error ' .*? '>'
    | '</error>'
    | '<fold ' .*? '>'
    | '</fold>'
    | '<TYPO ' .*? '>'
    | '</TYPO>'
    ) -> channel(HIDDEN)
    ;
NL
    : '\r'? '\n' -> channel(HIDDEN)
    ;
WS
    : [ \t]+ -> channel(HIDDEN)
    ;
LCURLY
    :   '{'
    ;
RCURLY
    :   '}'
    ;
LPAREN
    :   '('
    ;
RPAREN
    :   ')'
    ;
LSQUARE
    :   '['
    ;
RSQUARE
    :   ']'
    ;
LT
    :   '<'
    ;
GT
    :   '>'
    ;
COMMA
    :   ','
    ;
DOT
    :   '.'
    ;
COLON
    :   ':'
    ;
SEMICOLON
    : ';'
    ;
ASSIGN
    : '='
    ;
IDENT
    :   (ALPHA | UNDERSCORE) (ALPHA | DIGIT | UNDERSCORE)*
    ;
STRING_VALUE
    : BACKTICK_STRING
    | TRIPLE_DOUBLE_QUOTE_STRING
    ;
STRING_NAME
    : DOUBLE_QUOTE_STRING
    | SINGLE_QUOTE_STRING
    ;
INTEGER_VALUE
    : DEC_VALUE
    | HEX_VALUE
    | OCT_VALUE
    ;
FLOAT_VALUE
    : EXPONENT
    | FLOAT_LIT
    | MINUS? INF
    | NAN
    ;
fragment DOUBLE_QUOTE_STRING
    : '"' ( ESC_SEQ | ~('\\' | '"' | '\r' | '\n') )* '"'
    ;
fragment SINGLE_QUOTE_STRING
    : '\'' ( ESC_SEQ | ~('\\' | '\'' | '\r' | '\n') )* '\''
    ;
fragment BACKTICK_STRING
    : '`' ( ESC_SEQ | ~('\\' | '`') )* '`'
    ;
fragment TRIPLE_DOUBLE_QUOTE_STRING
    : '"""' TDQ_STRING_CHAR*? '"""'
    ;
fragment TDQ_STRING_CHAR
    :   ~["\\]
    |   '"' { !(_input.LA(1) == '"' && _input.LA(2) == '"') }?
    |   ESC_SEQ
    ;
fragment EXPONENT
    : (FLOAT_LIT|DEC_VALUE) EXP DEC_VALUE
    ;
fragment FLOAT_LIT
    : MINUS? DIGIT+ '.' DIGIT*     // "0.", "0.123"
    | MINUS? '.' DIGIT+            // ".123"
    ;
fragment INF
    : 'inf'
    ;
fragment NAN
    : 'nan'
    ;
fragment EXP
    : 'e'
    | 'E'
    ;
fragment DEC_VALUE
    : '0' | MINUS? '1'..'9' '0'..'9'*
    ;
fragment HEX_VALUE
    : MINUS? '0' [xX] HEX_DIGIT+
    ;
fragment OCT_VALUE
    : MINUS? '0' OCT_DIGIT+
    ;
fragment MINUS
    : '-'
    ;
fragment ALPHA
    : [a-zA-Z]
    ;
fragment DIGIT
    : [0-9]
    ;
fragment HEX_DIGIT
    : [0-9a-fA-F]
    ;
fragment OCT_DIGIT
    : [0-7]
    ;
fragment UNDERSCORE
    : '_'
    ;
fragment ESC_SEQ
    :   '\\' ('a'|'v'|'b'|'t'|'n'|'f'|'r'|'?'|'"'|'\''|'\\'|'`')
    |   '\\' ('x'|'X') HEX_DIGIT HEX_DIGIT
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;
fragment OCTAL_ESC
    :   '\\' [0-3] OCT_DIGIT OCT_DIGIT
    |   '\\' OCT_DIGIT OCT_DIGIT
    |   '\\' OCT_DIGIT
    ;
fragment UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

/** "catch all" rule for any char not matche in a token rule of your
 *  grammar. Lexers in Intellij must return all tokens good and bad.
 *  There must be a token to cover all characters, which makes sense, for
 *  an IDE. The parser however should not see these bad tokens because
 *  it just confuses the issue. Hence, the hidden channel.
 */
ERRCHAR
	:	.	-> channel(HIDDEN)
	;
