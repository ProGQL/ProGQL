grammar ProGQL;

@header {
	package antlr;
}

oC_ProGQL
      :  SP? oC_ProGQLQuery (SP (UNION | INTERSECT) SP '(' oC_ProGQLQuery ')')* ( SP? ';' )? SP? EOF ;


oC_ProGQLQuery
            :  SP? oC_SingleQuery (SP (INTERSECT | UNION) SP oC_With)? ;


UNION : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' )  ;


INTERSECT : ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'S' | 's' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )  ;

ALL : ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )  ;


oC_SingleQuery
           :  ( (SP? oC_Match) (SP (oC_BFS | oC_DFS))? SP oC_Yield SP oC_Return)	
               | (  ( (SP? oC_Match) (SP (oC_BFS | oC_DFS) SP oC_Yield SP oC_Unwind)? SP oC_UpdatingClause (SP oC_With)?)+ (SP oC_Yield)? SP oC_Return  )
               ;


oC_UpdatingClause
              :  oC_Create
                  | oC_Merge
                  | oC_Delete
                  | oC_Set
                  | oC_Remove
                  ;

oC_ReadingClause
             :  oC_Match
                 ;

oC_Match
     :  ( OPTIONAL SP )? MATCH SP? oC_Pattern ( SP? oC_Where )?
     	;

OPTIONAL : ( 'O' | 'o' ) ( 'P' | 'p' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'L' | 'l' )  ;

MATCH : ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'C' | 'c' ) ( 'H' | 'h' )  ;

oC_Unwind
      :  UNWIND SP? oC_Expression SP AS SP oC_Variable ;

UNWIND : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'W' | 'w' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'D' | 'd' )  ;

AS : ( 'A' | 'a' ) ( 'S' | 's' )  ;

oC_Merge
     :  MERGE SP? oC_PatternPart ( SP oC_MergeAction )* ;

MERGE : ( 'M' | 'm' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'G' | 'g' ) ( 'E' | 'e' )  ;

oC_MergeAction
           :  ( ON SP MATCH SP oC_Set )
               | ( ON SP CREATE SP oC_Set )
               ;

ON : ( 'O' | 'o' ) ( 'N' | 'n' )  ;

CREATE : ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

oC_Create
      :  CREATE SP? oC_Pattern ;

oC_Set
   :  SET SP? oC_SetItem ( ',' oC_SetItem )* ;

SET : ( 'S' | 's' ) ( 'E' | 'e' ) ( 'T' | 't' )  ;

oC_SetItem
       :  ( oC_PropertyExpression SP? '=' (SP? oC_Expression | SP? oC_Reduce))
       	   | ( oC_PropertyExpression SP? '=' (SP? oC_Expression | SP? oC_Projection))
           | ( oC_Variable SP? '=' (SP? oC_Expression | SP? oC_Reduce))
           | ( oC_Variable SP? '=' (SP? oC_Expression | SP? oC_Projection))
           | ( oC_Variable SP? '+=' SP? oC_Expression )
           | ( oC_Variable SP? oC_NodeLabels )
           ;

oC_Delete
      :  ( DETACH SP )? DELETE SP? oC_Expression ( SP? ',' SP? oC_Expression )* ;

DETACH : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'C' | 'c' ) ( 'H' | 'h' )  ;

DELETE : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

oC_Remove
      :  REMOVE SP oC_RemoveItem ( SP? ',' SP? oC_RemoveItem )* ;

REMOVE : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'O' | 'o' ) ( 'V' | 'v' ) ( 'E' | 'e' )  ;

oC_RemoveItem
          :  ( oC_Variable oC_NodeLabels )
              | oC_PropertyExpression
              ;
           
oC_Yield
           :  YIELD SP oC_YieldItems ;

CALL : ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )  ;

YIELD : ( 'Y' | 'y' ) ( 'I' | 'i' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'D' | 'd' )  ;

oC_StandaloneCall
              :  CALL SP ( oC_ExplicitProcedureInvocation | oC_ImplicitProcedureInvocation ) ( SP YIELD SP oC_YieldItems )? ;

oC_YieldItems
          :  ( '*' | ( oC_YieldItem ( SP? ',' SP? oC_YieldItem )* ) ) ( SP? oC_Where )? ;

oC_YieldItem
         :  ( oC_ProcedureResultField SP AS SP )? oC_Variable ;

   
oC_With
    :  SP? WITH SP? oC_Variable ( (SP? '=' SP? '(' SP? oC_Match SP? ')' SP (oC_BFS | oC_DFS) SP? oC_Yield SP oC_Return)
    	| (SP oC_Where) );

WITH : ( 'W' | 'w' ) ( 'I' | 'i' ) ( 'T' | 't' ) ( 'H' | 'h' )  ;

oC_Return
      :  RETURN SP oC_Variable ;

RETURN : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'U' | 'u' ) ( 'R' | 'r' ) ( 'N' | 'n' )  ;

oC_ProjectionBody
              :  ( SP? DISTINCT )? SP oC_ProjectionItems ( SP oC_Order )? ( SP oC_Skip )? ( SP oC_Limit )? ;

DISTINCT : ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' )  ;

oC_ProjectionItems
               :  ( '*' ( SP? ',' SP? oC_ProjectionItem )* )
                   | ( oC_ProjectionItem ( SP? ',' SP? oC_ProjectionItem )* )
                   ;

oC_ProjectionItem
              :  ( oC_Expression SP AS SP oC_Variable )
                  | oC_Expression
                  ;

oC_Order
     :  ORDER SP BY SP oC_SortItem ( ',' SP? oC_SortItem )* ;

ORDER : ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;

BY : ( 'B' | 'b' ) ( 'Y' | 'y' )  ;

oC_Skip
    :  L_SKIP SP oC_Expression ;

L_SKIP : ( 'S' | 's' ) ( 'K' | 'k' ) ( 'I' | 'i' ) ( 'P' | 'p' )  ;

oC_Limit
     :  LIMIT SP oC_Expression ;

LIMIT : ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' )  ;

oC_SortItem
        :  oC_Expression ( SP? ( ASCENDING | ASC | DESCENDING | DESC ) )? ;

ASCENDING : ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )  ;

ASC : ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' )  ;

DESCENDING : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )  ;

DESC : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' )  ;

oC_Where
     :  WHERE SP oC_Expression ( SP oC_Order )? ( SP oC_Skip )? ( SP oC_Limit )?;	//Add ( SP oC_Order )? ( SP oC_Skip )? ( SP oC_Limit )?

WHERE : ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )  ;

oC_BFS
	: BFS SP? '(' SP? oC_Variable SP IN ((SP? oC_Backward) | (SP? oC_Forward)) SP? '|' SP oC_Match SP? ')' ;

BFS : ( 'B' | 'b' ) ( 'F' | 'f' ) ( 'S' | 's' )  ;

oC_Backward
	: BACKWARD SP? '(' oC_Variable ')' ;
	
oC_Forward
	: FORWARD SP? '(' oC_Variable ')' ;

oC_DFS
	: DFS SP? '(' SP? oC_Variable SP IN ((SP? oC_Backward) | (SP? oC_Forward)) SP? '|' SP oC_Match SP? ')' ;

DFS : ( 'D' | 'd' ) ( 'F' | 'f' ) ( 'S' | 's' )  ;


oC_Max	//Add oC_Max
	: MAX SP? '(' SP? oC_Collect SP? ')' ;

MAX : ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'X' | 'x' ) ;

oC_Min	//Add oC_Min
	: MIN SP? '(' SP? oC_Collect SP? ')' ;

MIN : ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ;

oC_Collect	//Add oC_Collect
	: COLLECT SP? '(' SP? oC_IdInColl SP? '|' ( SP oC_PropertyExpression ) SP? ')' ;

COLLECT : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) ;

oC_Reduce	//Add oC_Reduce
	: REDUCE SP? '(' oC_Variable SP? '=' (SP? oC_NumberLiteral) SP? ',' SP? oC_IdInColl SP? '|' ( SP? oC_Expression ) SP? ')' ;

REDUCE : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'D' | 'd' ) ( 'U' | 'u' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ;

oC_Projection	//Add oC_Projection
	: PROJECTION SP? '(' (SP? oC_Expression (',')?)+  SP? ')' ;
	
PROJECTION : ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'J' | 'j' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ;

oC_Pattern
       :  (oC_PatternPart ( SP? ',' SP? oC_PatternPart )*)
       		| (oC_IdInColl)
       		| (oC_Expression);

oC_PatternPart
           :  ( oC_Variable SP? '=' SP? oC_AnonymousPatternPart )
               | oC_AnonymousPatternPart
               ;

oC_AnonymousPatternPart
                    :  oC_PatternElement ;

oC_PatternElement
              :  ( oC_NodePattern ( SP? oC_PatternElementChain )* )
                  | ( '(' oC_PatternElement ')' )
                  ;

oC_NodePattern
           :  '(' SP? ( oC_Variable SP? )? ( oC_NodeLabels SP? )? ( oC_Properties SP? )? ')' ;

oC_PatternElementChain
                   :  oC_RelationshipPattern SP? oC_NodePattern ;

oC_RelationshipPattern
                   :  ( oC_LeftArrowHead SP? oC_Dash SP? oC_RelationshipDetail? SP? oC_Dash SP? oC_RightArrowHead )
                       | ( oC_LeftArrowHead SP? oC_Dash SP? oC_RelationshipDetail? SP? oC_Dash )
                       | ( oC_Dash SP? oC_RelationshipDetail? SP? oC_Dash SP? oC_RightArrowHead )
                       | ( oC_Dash SP? oC_RelationshipDetail? SP? oC_Dash )
                       ;

oC_RelationshipDetail
                  :  '[' SP? ( oC_Variable SP? )? ( oC_RelationshipTypes SP? )? oC_RangeLiteral? ( oC_Properties SP? )? ']' ;

oC_Properties
          :  oC_MapLiteral
              | oC_Parameter
              ;

oC_RelationshipTypes
                 :  ':' SP? oC_RelTypeName ( SP? '|' ':'? SP? oC_RelTypeName )* ;

oC_NodeLabels
          :  oC_NodeLabel ( SP? oC_NodeLabel )* ;

oC_NodeLabel
         :  ':' SP? oC_LabelName ;

oC_RangeLiteral
            :  '*' SP? ( oC_IntegerLiteral SP? )? ( '..' SP? ( oC_IntegerLiteral SP? )? )? ;

oC_LabelName
         :  oC_SchemaName ;

oC_RelTypeName
           :  oC_SchemaName ;

oC_Expression
          :  oC_OrExpression ;

oC_OrExpression
            :  oC_XorExpression ( SP OR SP oC_XorExpression )* ;

OR : ( 'O' | 'o' ) ( 'R' | 'r' )  ;

oC_XorExpression
             :  oC_AndExpression ( SP XOR SP oC_AndExpression )* ;

XOR : ( 'X' | 'x' ) ( 'O' | 'o' ) ( 'R' | 'r' )  ;

oC_AndExpression
             :  oC_NotExpression ( SP AND SP oC_NotExpression )* ;

AND : ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )  ;

oC_NotExpression
             :  ( NOT SP? )* oC_ComparisonExpression ;

NOT : ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )  ;

oC_ComparisonExpression
                    :  oC_AddOrSubtractExpression ( SP? oC_PartialComparisonExpression )* ;

oC_AddOrSubtractExpression
                       :  oC_MultiplyDivideModuloExpression ( ( SP? '+' SP? oC_MultiplyDivideModuloExpression ) | ( SP? '-' SP? oC_MultiplyDivideModuloExpression ) )* ;
                       
oC_TraversalExpression	//Add oC_TraversalExpression
						:  oC_Max | oC_Min;

oC_MultiplyDivideModuloExpression
                              :  oC_PowerOfExpression ( ( SP? '*' SP? oC_PowerOfExpression ) | ( SP? '/' SP? oC_PowerOfExpression ) | ( SP? '%' SP? oC_PowerOfExpression ) )* ;

oC_PowerOfExpression
                 :  oC_UnaryAddOrSubtractExpression ( SP? '^' SP? oC_UnaryAddOrSubtractExpression )* ;

oC_UnaryAddOrSubtractExpression
                            :  ( ( '+' | '-' ) SP? )* oC_StringListNullOperatorExpression ;

oC_StringListNullOperatorExpression
                                :  oC_PropertyOrLabelsExpression ( oC_StringOperatorExpression | oC_ListOperatorExpression | oC_NullOperatorExpression )* ;

oC_ListOperatorExpression
                      :  ( SP IN SP? oC_PropertyOrLabelsExpression )
                          | ( SP? '[' oC_Expression ']' )
                          | ( SP? '[' oC_Expression? '..' oC_Expression? ']' )
                          ;

IN : ( 'I' | 'i' ) ( 'N' | 'n' )  ;

oC_StringOperatorExpression
                        :  ( ( SP STARTS SP WITH ) | ( SP ENDS SP WITH ) | ( SP CONTAINS ) ) SP? oC_PropertyOrLabelsExpression ;

STARTS : ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'S' | 's' )  ;

ENDS : ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'S' | 's' )  ;

CONTAINS : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' )  ;

oC_NullOperatorExpression
                      :  ( SP IS SP NULL )
                          | ( SP IS SP NOT SP NULL )
                          ;

IS : ( 'I' | 'i' ) ( 'S' | 's' )  ;

NULL : ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )  ;

oC_PropertyOrLabelsExpression: oC_Atom ( SP? oC_PropertyLookup )* ( SP? oC_NodeLabels )? # PropOrLblDeclaration
                            ;	


oC_Atom
    :  oC_Literal
        | oC_Parameter
        | oC_CaseExpression
        | ( COUNT SP? '(' SP? '*' SP? ')' )
        | oC_ListComprehension
        | oC_PatternComprehension
        | ( ALL SP? '(' SP? oC_FilterExpression SP? ')' )
        | ( ANY SP? '(' SP? oC_FilterExpression SP? ')' )
        | ( NONE SP? '(' SP? oC_FilterExpression SP? ')' )
        | ( SINGLE SP? '(' SP? oC_FilterExpression SP? ')' )
        | oC_RelationshipsPattern
        | oC_ParenthesizedExpression
        | oC_FunctionInvocation
        | oC_Variable
        ;

COUNT : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'T' | 't' )  ;

BACKWARD : ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'C' | 'c' ) ( 'K' | 'k' ) ( 'W' | 'w' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'D' | 'd' )  ;

FORWARD : ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'W' | 'w' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'D' | 'd' )  ;

DST : ( 'D' | 'd' ) ( 'S' | 's' ) ( 'T' | 't' )  ;

SRC : ( 'S' | 's' ) ( 'R' | 'r' ) ( 'C' | 'c' )  ;

OUT : ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' )  ;

ANY : ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' )  ;

NONE : ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'E' | 'e' )  ;

SINGLE : ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' ) ( 'L' | 'l' ) ( 'E' | 'e' )  ;

oC_Literal
       :  oC_NumberLiteral
           | StringLiteral
           | oC_BooleanLiteral
           | NULL
           | oC_MapLiteral
           | oC_ListLiteral
           ;

oC_BooleanLiteral
              :  TRUE
                  | FALSE
                  ;

TRUE : ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )  ;

FALSE : ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )  ;

oC_ListLiteral
           :  '[' SP? ( oC_Expression SP? ( ',' SP? oC_Expression SP? )* )? ']' ;

oC_PartialComparisonExpression
                           :  ( '=' SP? oC_AddOrSubtractExpression | SP? oC_TraversalExpression)
                               | ( '<>' SP? oC_AddOrSubtractExpression | SP? oC_TraversalExpression)
                               | ( '<' (SP? oC_AddOrSubtractExpression  | SP? oC_TraversalExpression))
                               | ( '>' (SP? oC_AddOrSubtractExpression | SP? oC_TraversalExpression))
                               | ( '<=' SP? oC_AddOrSubtractExpression | SP? oC_TraversalExpression)
                               | ( '>=' SP? oC_AddOrSubtractExpression | SP? oC_TraversalExpression)
                               ;

oC_ParenthesizedExpression
                       :  '(' SP? oC_Expression SP? ')' ;

oC_RelationshipsPattern
                    :  oC_NodePattern ( SP? oC_PatternElementChain )+ ;

oC_FilterExpression
                :  oC_IdInColl ( SP? oC_Where )? ;

oC_IdInColl
        :  oC_Variable SP IN SP oC_Expression ;

oC_FunctionInvocation
                  :  oC_FunctionName SP? '(' SP? ( DISTINCT SP? )? ( oC_Expression SP? ( ',' SP? oC_Expression SP? )* )? ')' ;

oC_FunctionName
            :  ( oC_Namespace oC_SymbolicName )
                | EXISTS
                ;

EXISTS : ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'S' | 's' )  ;

oC_ExplicitProcedureInvocation
                           :  oC_ProcedureName SP? '(' SP? ( oC_Expression SP? ( ',' SP? oC_Expression SP? )* )? ')' ;

oC_ImplicitProcedureInvocation
                           :  oC_ProcedureName ;

oC_ProcedureResultField
                    :  oC_SymbolicName ;

oC_ProcedureName
             :  oC_Namespace oC_SymbolicName ;
  
oC_Namespace
         :  ( oC_SymbolicName '.' )* ;

oC_ListComprehension
                 :  '[' SP? oC_FilterExpression ( SP? '|' SP? oC_Expression )? SP? ']' ;

oC_PatternComprehension
                    :  '[' SP? ( oC_Variable SP? '=' SP? )? oC_RelationshipsPattern SP? ( WHERE SP? oC_Expression SP? )? '|' SP? oC_Expression SP? ']' ;

oC_PropertyLookup
              :  '.' SP? ( oC_PropertyKeyName ) ;

oC_CaseExpression
              :  ( ( CASE ( SP? oC_CaseAlternatives )+ ) | ( CASE SP? oC_Expression ( SP? oC_CaseAlternatives )+ ) ) ( SP? ELSE SP? oC_Expression )? SP? END ;

CASE : ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )  ;

ELSE : ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )  ;

END : ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' )  ;

oC_CaseAlternatives
                :  WHEN SP? oC_Expression SP? THEN SP? oC_Expression ;

WHEN : ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'N' | 'n' )  ;

THEN : ( 'T' | 't' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'N' | 'n' )  ;

oC_Variable
        :  oC_SymbolicName ;

StringLiteral
             :  ( '"' ( StringLiteral_0 | EscapedChar )* '"' )
                 | ( '\'' ( StringLiteral_1 | EscapedChar )* '\'' )
                 ;

EscapedChar
           :  '\\' ( '\\' | '\'' | '"' | ( 'B' | 'b' ) | ( 'F' | 'f' ) | ( 'N' | 'n' ) | ( 'R' | 'r' ) | ( 'T' | 't' ) | ( ( 'U' | 'u' ) ( HexDigit HexDigit HexDigit HexDigit ) ) | ( ( 'U' | 'u' ) ( HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit ) ) ) ;

oC_NumberLiteral
             :  oC_DoubleLiteral
                 | oC_IntegerLiteral
                 ;

oC_MapLiteral
          :  '{' SP? ( oC_PropertyKeyName SP? ':' SP? oC_Expression SP? ( ',' SP? oC_PropertyKeyName SP? ':' SP? oC_Expression SP? )* )? '}' ;

oC_Parameter
         :  '$' ( oC_SymbolicName | DecimalInteger ) ;

oC_PropertyExpression
                  :  oC_Atom ( SP? oC_PropertyLookup )+ ;

oC_PropertyKeyName
               :  oC_SchemaName ;

oC_IntegerLiteral
              :  HexInteger
                  | OctalInteger
                  | DecimalInteger
                  ;

HexInteger
          :  '0x' ( HexDigit )+ ;

DecimalInteger
              :  ZeroDigit
                  | ( NonZeroDigit ( Digit )* )
                  ;

OctalInteger
            :  ZeroDigit ( OctDigit )+ ;

HexLetter
         :  ( 'A' | 'a' )
             | ( 'B' | 'b' )
             | ( 'C' | 'c' )
             | ( 'D' | 'd' )
             | ( 'E' | 'e' )
             | ( 'F' | 'f' )
             ;

HexDigit
        :  Digit
            | HexLetter
            ;

Digit
     :  ZeroDigit
         | NonZeroDigit
         ;

NonZeroDigit
            :  NonZeroOctDigit
                | '8'
                | '9'
                ;

NonZeroOctDigit
               :  '1'
                   | '2'
                   | '3'
                   | '4'
                   | '5'
                   | '6'
                   | '7'
                   ;

OctDigit
        :  ZeroDigit
            | NonZeroOctDigit
            ;

ZeroDigit
         :  '0' ;

oC_DoubleLiteral
             :  ExponentDecimalReal
                 | RegularDecimalReal
                 ;

ExponentDecimalReal
                   :  ( ( Digit )+ | ( ( Digit )+ '.' ( Digit )+ ) | ( '.' ( Digit )+ ) ) ( 'E' | 'e' ) '-'? ( Digit )+ ;

RegularDecimalReal
                  :  ( Digit )* '.' ( Digit )+ ;

oC_SchemaName
          :  oC_SymbolicName
              | oC_ReservedWord
              ;

oC_ReservedWord
            :  ALL
                | ASC
                | ASCENDING
                | BY
                | CREATE
                | DELETE
                | DESC
                | DESCENDING
                | DETACH
                | EXISTS
                | LIMIT
                | MATCH
                | MERGE
                | ON
                | OPTIONAL
                | ORDER
                | REMOVE
                | RETURN
                | SET
                | L_SKIP
                | WHERE
                | BFS	//Add BFS
                | DFS 	//Add DFS
                | BACKWARD	//Add BACKWARD
                | FORWARD	//Add FORWARD
                | DST	//Add DST
                | SRC	//Add SRC
                | COLLECT	//Add COLLECT
                | REDUCE	//Add REDUCE
                | PROJECTION	//Add PROJECTION
                | OUT	//Add OUT
                | WITH
                | UNION
                | INTERSECT	//Add INTERSECT
                | UNWIND
                | AND
                | AS
                | CONTAINS
                | DISTINCT
                | ENDS
                | IN
                | IS
                | NOT
                | OR
                | STARTS
                | XOR
                | FALSE
                | TRUE
                | NULL
                | CONSTRAINT
                | DO
                | FOR
                | REQUIRE
                | UNIQUE
                | CASE
                | WHEN
                | THEN
                | ELSE
                | END
                | MANDATORY
                | SCALAR
                | OF
                | ADD
                | DROP
                ;

CONSTRAINT : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' )  ;

DO : ( 'D' | 'd' ) ( 'O' | 'o' )  ;

FOR : ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'R' | 'r' )  ;

REQUIRE : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'Q' | 'q' ) ( 'U' | 'u' ) ( 'I' | 'i' ) ( 'R' | 'r' ) ( 'E' | 'e' )  ;

UNIQUE : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'Q' | 'q' ) ( 'U' | 'u' ) ( 'E' | 'e' )  ;

MANDATORY : ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'Y' | 'y' )  ;

SCALAR : ( 'S' | 's' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'R' | 'r' )  ;

OF : ( 'O' | 'o' ) ( 'F' | 'f' )  ;

ADD : ( 'A' | 'a' ) ( 'D' | 'd' ) ( 'D' | 'd' )  ;

DROP : ( 'D' | 'd' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'P' | 'p' )  ;

oC_SymbolicName
            :  UnescapedSymbolicName
                | EscapedSymbolicName
                | HexLetter
                | COUNT
                | FILTER
                | EXTRACT
                | ANY
                | NONE
                | SINGLE
                | DST
                | SRC
                | OUT
                | IN
                ;

FILTER : ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;

EXTRACT : ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'T' | 't' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'C' | 'c' ) ( 'T' | 't' )  ;

UnescapedSymbolicName
                     :  IdentifierStart ( IdentifierPart )* ;

/**
 * Based on the unicode identifier and pattern syntax
 *   (http://www.unicode.org/reports/tr31/)
 * And extended with a few characters.
 */
IdentifierStart
               :  ID_Start
                   | Pc
                   ;

/**
 * Based on the unicode identifier and pattern syntax
 *   (http://www.unicode.org/reports/tr31/)
 * And extended with a few characters.
 */
IdentifierPart
              :  ID_Continue
                  | Sc
                  ;

/**
 * Any character except "`", enclosed within `backticks`. Backticks are escaped with double backticks.
 */
EscapedSymbolicName
                   :  ( '`' ( EscapedSymbolicName_0 )* '`' )+ ;

SP
  :  ( WHITESPACE )+ ;

WHITESPACE
          :  SPACE
              | TAB
              | LF
              | VT
              | FF
              | CR
              | FS
              | GS
              | RS
              | US
              | '\u1680'
              | '\u180e'
              | '\u2000'
              | '\u2001'
              | '\u2002'
              | '\u2003'
              | '\u2004'
              | '\u2005'
              | '\u2006'
              | '\u2008'
              | '\u2009'
              | '\u200a'
              | '\u2028'
              | '\u2029'
              | '\u205f'
              | '\u3000'
              | '\u00a0'
              | '\u2007'
              | '\u202f'
              | Comment
              ;

Comment
       :  ( '/*' ( Comment_1 | ( '*' Comment_2 ) )* '*/' )
           | ( '//' ( Comment_3 )* CR? ( LF | EOF ) )
           ;

oC_LeftArrowHead
             :  '<'
                 | '\u27e8'
                 | '\u3008'
                 | '\ufe64'
                 | '\uff1c'
                 ;

oC_RightArrowHead
              :  '>'
                  | '\u27e9'
                  | '\u3009'
                  | '\ufe65'
                  | '\uff1e'
                  ;

oC_Dash
    :  '-'
        | '\u00ad'
        | '\u2010'
        | '\u2011'
        | '\u2012'
        | '\u2013'
        | '\u2014'
        | '\u2015'
        | '\u2212'
        | '\ufe58'
        | '\ufe63'
        | '\uff0d'
        ;

fragment FF : [\f] ;

fragment EscapedSymbolicName_0 : ~[`] ;

fragment RS : [\u001E] ;

fragment ID_Continue : [\p{ID_Continue}] ;

fragment Comment_1 : ~[*] ;

fragment StringLiteral_1 : ~['\\] ;

fragment Comment_3 : ~[\n\r] ;

fragment Comment_2 : ~[/] ;

fragment GS : [\u001D] ;

fragment FS : [\u001C] ;

fragment CR : [\r] ;

fragment Sc : [\p{Sc}] ;

fragment SPACE : [ ] ;

fragment Pc : [\p{Pc}] ;

fragment TAB : [\t] ;

fragment StringLiteral_0 : ~["\\] ;

fragment LF : [\n] ;

fragment VT : [\u000B] ;

fragment US : [\u001F] ;

fragment ID_Start : [\p{ID_Start}] ;


