parser grammar ProtoParser;

options {
    tokenVocab = ProtoLexer;
}

proto
    // syntax should be first statement in the file
    : syntaxStatement?
        ( packageStatement
        | importStatement
        | optionEntry
        | enumBlock
        | messageBlock
        | extendBlock
        | serviceBlock)*
    EOF
    ;
syntaxStatement
    : SYNTAX ASSIGN syntaxName SEMICOLON
    ;
syntaxName
    : STRING_NAME
    ;
packageStatement
    : PACKAGE packageName SEMICOLON
    ;
packageName
    : fullIdent
    ;
importStatement
    : IMPORT PUBLIC? fileReference SEMICOLON
    ;
fileReference
    : STRING_NAME
    ;
optionEntry
    : OPTION option SEMICOLON
    ;
enumBlock
    : ENUM enumName LCURLY
        (enumField
        | optionEntry
        | reservedFieldRanges
        | reservedFieldNames)*
      RCURLY SEMICOLON?
    ;
enumName
    : ident
    ;
enumField
    : enumFieldName ASSIGN enumFieldValue fieldOptions? SEMICOLON
    ;
enumFieldName
    : ident
    ;
enumFieldValue
    : INTEGER_VALUE
    ;
extendBlock
    : EXTEND typeReference LCURLY extendBlockEntry* RCURLY SEMICOLON?
    ;
extendBlockEntry
    : field
    | groupBlock
    ;
serviceBlock
    : SERVICE serviceName LCURLY (rpcMethod | optionEntry)* RCURLY SEMICOLON?
    ;
serviceName
    : ident
    ;
rpcMethod
    : RPC rpcName LPAREN rpcType  RPAREN
      RETURNS LPAREN rpcType RPAREN (LCURLY optionEntry* RCURLY)? SEMICOLON?
    ;
rpcName
    : ident
    ;
rpcType
    : STREAM? typeReference
    ;
messageBlock
    : MESSAGE messageName LCURLY
        (field
        | optionEntry
        | messageBlock
        | enumBlock
        | extensions
        | extendBlock
        | groupBlock
        | oneof
        | map
        | reservedFieldRanges
        | reservedFieldNames)*
      RCURLY SEMICOLON?
    ;
messageName
    : ident
    ;
oneof
    : ONEOF oneofName LCURLY (field | groupBlock | optionEntry)* RCURLY SEMICOLON?
    ;
oneofName
    : ident
    ;
map
    : MAP LT mapKey COMMA mapValue GT fieldName ASSIGN tag fieldOptions? SEMICOLON
    ;
mapKey
    : INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | BOOL
    | STRING
    ;
mapValue
    : typeReference
    ;
tag
    : INTEGER_VALUE
    ;
groupBlock
    : fieldModifier? GROUP groupName ASSIGN tag LCURLY
        (field
        | optionEntry
        | messageBlock
        | enumBlock
        | extensions
        | extendBlock
        | groupBlock)*
    RCURLY SEMICOLON?
    ;
groupName
    : ident
    ;
extensions
    : EXTENSIONS range (COMMA range)* SEMICOLON
    ;
range
    : rangeFrom ( TO ( rangeTo | MAX ) )?
    ;
rangeFrom
    : INTEGER_VALUE
    ;
rangeTo
    : INTEGER_VALUE
    ;
reservedFieldRanges
    : RESERVED range (COMMA range)* SEMICOLON
    ;
reservedFieldNames
    : RESERVED reservedFieldName (COMMA reservedFieldName)* SEMICOLON
    ;
reservedFieldName
    : STRING_NAME
    ;
field
    : fieldModifier? typeReference fieldName ASSIGN tag fieldOptions? SEMICOLON
    ;
fieldName
    : ident
    ;
fieldModifier
    : OPTIONAL
    | REQUIRED
    | REPEATED
    ;
typeReference
    : DOUBLE
    | FLOAT
    | INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | BOOL
    | STRING
    | BYTES
    | DOT? ident (DOT ident)*
    ;
fieldOptions
    : LSQUARE (option (COMMA option)* )? RSQUARE
    ;
option
    : fieldRerefence ASSIGN optionValue
    ;
fieldRerefence
    : standardFieldRerefence | LPAREN customFieldReference RPAREN
    (DOT (standardFieldRerefence | LPAREN customFieldReference RPAREN))*
    ;
standardFieldRerefence
    : ident
    ;
customFieldReference
    : DOT? ident (DOT ident)*
    ;
optionValue
    : INTEGER_VALUE
    | FLOAT_VALUE
    | BOOLEAN_VALUE
    | STRING_NAME
    | STRING_VALUE
    | IDENT
    | textFormat
    ;
textFormat
    : LCURLY textFormatEntry* RCURLY
    ;
textFormatOptionName
    : ident
    | LSQUARE typeReference RSQUARE
    ;
textFormatEntry
    : textFormatOptionName COLON textFormatOptionValue
    | textFormatOptionName textFormat
    ;
textFormatOptionValue
    : INTEGER_VALUE
    | FLOAT_VALUE
    | BOOLEAN_VALUE
    | STRING_NAME
    | STRING_VALUE
    | IDENT
    ;
fullIdent
    : ident (DOT ident)*
    ;
ident
    : IDENT
    | PACKAGE
    | SYNTAX
    | IMPORT
    | PUBLIC
    | OPTION
    | MESSAGE
    | GROUP
    | OPTIONAL
    | REQUIRED
    | REPEATED
    | ONEOF
    | EXTEND
    | EXTENSIONS
    | TO
    | MAX
    | RESERVED
    | ENUM
    | SERVICE
    | RPC
    | RETURNS
    | STREAM
    | MAP
    | BOOLEAN_VALUE
    | DOUBLE
    | FLOAT
    | INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | BOOL
    | STRING
    | BYTES
    ;
