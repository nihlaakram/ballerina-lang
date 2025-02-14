/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.BLangBuiltInMethod;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Some utils methods for building AST nodes at desugar phase.
 *
 * @since 0.965.0
 */
public class ASTBuilderUtil {

    /**
     * Prepend generated code to given body.
     *
     * @param generatedCode generated code.
     * @param target        prepend target
     */
    static void prependStatements(BLangBlockStmt generatedCode, BLangBlockStmt target) {
        int index = 0;
        for (BLangStatement stmt : generatedCode.stmts) {
            target.stmts.add(index++, stmt);
        }
    }

    /**
     * Append generated code to given body.
     *
     * @param generatedCode generated code.
     * @param target        prepend target
     */
    static void appendStatements(BLangBlockStmt generatedCode, BLangBlockStmt target) {
        int index = 0;
        if (target.stmts.get(target.stmts.size() - 1).getKind() == NodeKind.RETURN) {
            index = target.stmts.size() - 1;
        }
        for (BLangStatement stmt : generatedCode.stmts) {
            target.stmts.add(index++, stmt);
        }
    }

    static void appendStatement(BLangStatement stmt, BLangBlockStmt target) {
        int index = 0;
        if (target.stmts.size() > 0 && target.stmts.get(target.stmts.size() - 1).getKind() == NodeKind.RETURN) {
            index = target.stmts.size() - 1;
        }
        target.stmts.add(index, stmt);
    }

    static void defineVariable(BLangSimpleVariable variable, BSymbol targetSymbol, Names names) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), targetSymbol.pkgID, variable.type,
                targetSymbol);
        targetSymbol.scope.define(variable.symbol.name, variable.symbol);
    }

    private static boolean isValueType(BType type) {
        return type.tag < TypeTags.JSON;
    }

    static BLangExpression wrapToConversionExpr(BType sourceType, BLangExpression exprToWrap,
                                                SymbolTable symTable, Types types) {
        if (types.isSameType(sourceType, exprToWrap.type) || !isValueType(exprToWrap.type)) {
            // No conversion needed.
            return exprToWrap;
        }
        BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        castExpr.expr = exprToWrap;
        castExpr.conversionSymbol = createUnboxValueOpSymbolToAnyType(exprToWrap.type, symTable);
        castExpr.type = symTable.anyType;
        castExpr.targetType = castExpr.type;
        return castExpr;
    }

    private static BCastOperatorSymbol createUnboxValueOpSymbolToAnyType(BType sourceType, SymbolTable symTable) {
        int opcode;
        switch (sourceType.tag) {
            case TypeTags.INT:
                opcode = InstructionCodes.I2ANY;
                break;
            case TypeTags.BYTE:
                opcode = InstructionCodes.BI2ANY;
                break;
            case TypeTags.FLOAT:
                opcode = InstructionCodes.F2ANY;
                break;
            case TypeTags.STRING:
                opcode = InstructionCodes.S2ANY;
                break;
            case TypeTags.DECIMAL:
                opcode = InstructionCodes.NOP;
                break;
            default:
                opcode = InstructionCodes.B2ANY;
                break;
        }

        List<BType> paramTypes = Lists.of(sourceType, symTable.anyType);
        BInvokableType opType = new BInvokableType(paramTypes, symTable.anyType, null);
        return new BCastOperatorSymbol(null, opType, sourceType, null, false, true, opcode);
    }

    static BLangFunction createFunction(DiagnosticPos pos, String name) {
        final BLangFunction bLangFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        final IdentifierNode funcName = createIdentifier(pos, name);
        bLangFunction.setName(funcName);
        bLangFunction.flagSet = EnumSet.of(Flag.LAMBDA);
        bLangFunction.pos = pos;
        // Create body of the function
        bLangFunction.body = createBlockStmt(pos);
        return bLangFunction;
    }

    static BLangType createTypeNode(BType type) {
        BLangType bLangType = new BLangType() {
            @Override
            public void accept(BLangNodeVisitor visitor) {
            }

            @Override
            public NodeKind getKind() {
                return null;
            }
        };
        bLangType.type = type;
        return bLangType;
    }

    static BLangIf createIfStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        target.addStatement(ifNode);
        return ifNode;
    }

    static BLangIf createIfElseStmt(DiagnosticPos pos,
                                    BLangExpression conditionExpr,
                                    BLangBlockStmt thenBody,
                                    BLangStatement elseStmt) {
        final BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        ifNode.expr = conditionExpr;
        ifNode.body = thenBody;
        ifNode.elseStmt = elseStmt;
        return ifNode;
    }

    static BLangTypeTestExpr createTypeTestExpr(DiagnosticPos pos, BLangExpression expr, BLangType type) {
        final BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        typeTestExpr.pos = pos;
        typeTestExpr.expr = expr;
        typeTestExpr.typeNode = type;
        return typeTestExpr;
    }

    static BLangForeach createForeach(DiagnosticPos pos,
                                      BLangBlockStmt target,
                                      BLangSimpleVarRef collectionVarRef) {
        final BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = pos;
        target.addStatement(foreach);
        foreach.body = ASTBuilderUtil.createBlockStmt(pos);
        foreach.collection = collectionVarRef;
        return foreach;
    }

    static BLangSimpleVariableDef createVariableDefStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangSimpleVariableDef variableDef = createVariableDef(pos);
        target.addStatement(variableDef);
        return variableDef;
    }

    static BLangSimpleVariableDef createVariableDef(DiagnosticPos pos) {
        final BLangSimpleVariableDef variableDef =
                (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        variableDef.pos = pos;
        return variableDef;
    }

    static BLangAssignment createAssignmentStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        target.addStatement(assignment);
        return assignment;
    }

    static BLangAssignment createAssignmentStmt(DiagnosticPos pos, BLangExpression varRef, BLangExpression rhsExpr) {
        return createAssignmentStmt(pos, varRef, rhsExpr, false);
    }

    static BLangAssignment createAssignmentStmt(DiagnosticPos pos, BLangExpression varRef,
                                                BLangExpression rhsExpr, boolean declaredWithVar) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        assignment.varRef = varRef;
        assignment.expr = rhsExpr;
        assignment.declaredWithVar = declaredWithVar;
        return assignment;
    }

    static BLangExpressionStmt createExpressionStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        target.addStatement(exprStmt);
        return exprStmt;
    }

    static BLangReturn createReturnStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        target.addStatement(returnStmt);
        return returnStmt;
    }

    public static BLangReturn createNilReturnStmt(DiagnosticPos pos, BType nilType) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = createLiteral(pos, nilType, Names.NIL_VALUE);
        return returnStmt;
    }

    public static BLangReturn createReturnStmt(DiagnosticPos pos, BType returnType, Object value) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = createLiteral(pos, returnType, value);
        return returnStmt;
    }

    public static BLangReturn createReturnStmt(DiagnosticPos pos, BLangExpression expr) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = expr;
        return returnStmt;
    }

    static void createContinueStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangContinue nextStmt = (BLangContinue) TreeBuilder.createContinueNode();
        nextStmt.pos = pos;
        target.addStatement(nextStmt);
    }

    static BLangBlockStmt createBlockStmt(DiagnosticPos pos) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        return blockNode;
    }

    static BLangBlockStmt createBlockStmt(DiagnosticPos pos, List<BLangStatement> stmts) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        blockNode.stmts = stmts;
        return blockNode;
    }

    static BLangMatch.BLangMatchTypedBindingPatternClause createMatchStatementPattern(DiagnosticPos pos,
                                                                                      BLangSimpleVariable variable,
                                                                                      BLangBlockStmt body) {
        BLangMatch.BLangMatchTypedBindingPatternClause patternClause =
                (BLangMatch.BLangMatchTypedBindingPatternClause)
                        TreeBuilder.createMatchStatementSimpleBindingPattern();
        patternClause.pos = pos;
        patternClause.variable = variable;
        patternClause.body = body;
        return patternClause;

    }

    static BLangMatch createMatchStatement(DiagnosticPos pos,
                                           BLangExpression expr,
                                           List<BLangMatch.BLangMatchTypedBindingPatternClause> patternClauses) {
        BLangMatch matchStmt = (BLangMatch) TreeBuilder.createMatchStatement();
        matchStmt.pos = pos;
        matchStmt.expr = expr;
        matchStmt.patternClauses.addAll(patternClauses);
        return matchStmt;
    }

    static BLangUnaryExpr createUnaryExpr(DiagnosticPos pos) {
        return createUnaryExpr(pos, null, null, null, null);
    }

    static BLangUnaryExpr createUnaryExpr(DiagnosticPos pos,
                                          BLangExpression expr,
                                          BType type,
                                          OperatorKind kind,
                                          BOperatorSymbol symbol) {
        final BLangUnaryExpr unaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpr.pos = pos;
        unaryExpr.expr = expr;
        unaryExpr.type = type;
        unaryExpr.operator = kind;
        unaryExpr.opSymbol = symbol;
        return unaryExpr;
    }

    static BLangTypedescExpr createTypeofExpr(DiagnosticPos pos, BType type, BType resolvedType) {
        final BLangTypedescExpr typeofExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typeofExpr.pos = pos;
        typeofExpr.type = type;
        typeofExpr.resolvedType = resolvedType;
        return typeofExpr;
    }

    static BLangIndexBasedAccess createIndexBasesAccessExpr(DiagnosticPos pos, BType type, BVarSymbol varSymbol,
                                                            BLangExpression indexExpr) {
        final BLangIndexBasedAccess arrayAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        arrayAccess.pos = pos;
        arrayAccess.expr = createVariableRef(pos, varSymbol);
        arrayAccess.indexExpr = indexExpr;
        arrayAccess.type = type;
        return arrayAccess;
    }

    static BLangExpression generateConversionExpr(BLangExpression varRef, BType target, SymbolResolver symResolver) {
        if (varRef.type.tag == target.tag || varRef.type.tag > TypeTags.BOOLEAN) {
            return varRef;
        }
        // Box value using cast expression.
        final BLangTypeConversionExpr conversion = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversion.pos = varRef.pos;
        conversion.expr = varRef;
        conversion.type = target;
        conversion.targetType = target;
        conversion.conversionSymbol = (BCastOperatorSymbol) symResolver.resolveCastOperator(varRef, varRef.type,
                                                                                            target);
        return conversion;
    }

    static BLangInvocation.BLangBuiltInMethodInvocation createBuiltInMethod(DiagnosticPos pos,
                                                                            BLangExpression expr,
                                                                            BInvokableSymbol invokableSymbol,
                                                                            List<BLangExpression> requiredArgs,
                                                                            SymbolResolver symResolver,
                                                                            BLangBuiltInMethod builtInFunction) {
        BLangInvocation invokeLambda = createInvocationExprMethod(pos, invokableSymbol, requiredArgs,
                                                                  new ArrayList<>(), symResolver);
        invokeLambda.expr = expr;
        return new BLangInvocation.BLangBuiltInMethodInvocation(invokeLambda, builtInFunction);
    }

    static List<BLangExpression> generateArgExprs(DiagnosticPos pos, List<BLangSimpleVariable> args,
                                                  List<BVarSymbol> formalParams, SymbolResolver symResolver) {
        List<BLangExpression> argsExpr = new ArrayList<>();
        final List<BLangSimpleVarRef> variableRefList = createVariableRefList(pos, args);
        for (int i = 0; i < variableRefList.size(); i++) {
            BLangSimpleVarRef varRef = variableRefList.get(i);
            BType target = formalParams.get(i).type;
            BType source = varRef.symbol.type;
            if (source != target) {
                argsExpr.add(generateConversionExpr(varRef, target, symResolver));
                continue;
            }
            argsExpr.add(varRef);
        }
        return argsExpr;
    }

    public static BLangInvocation createInvocationExpr(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                List<BLangSimpleVariable> requiredArgs, SymbolResolver symResolver) {
        return createInvocationExpr(pos, invokableSymbol, requiredArgs, new ArrayList<>(), symResolver);
    }

    static BLangInvocation createInvocationExpr(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                List<BLangSimpleVariable> requiredArgs,
                                                List<BLangSimpleVariable> restArgs, SymbolResolver symResolver) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        invokeLambda.requiredArgs.addAll(generateArgExprs(pos, requiredArgs, invokableSymbol.params, symResolver));
        invokeLambda.restArgs
                .addAll(generateArgExprs(pos, restArgs, Lists.of(invokableSymbol.restParam), symResolver));

        invokeLambda.symbol = invokableSymbol;
        invokeLambda.type = ((BInvokableType) invokableSymbol.type).retType;
        return invokeLambda;
    }

    static BLangInvocation createInvocationExprForMethod(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                List<BLangExpression> requiredArgs, SymbolResolver symResolver) {
        return createInvocationExprMethod(pos, invokableSymbol, requiredArgs, new ArrayList<>(), symResolver);
    }

    static BLangInvocation createInvocationExprMethod(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                      List<BLangExpression> requiredArgs,
                                                      List<BLangSimpleVariable> restArgs, SymbolResolver symResolver) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        invokeLambda.name = createIdentifier(pos, invokableSymbol.name.value);
        invokeLambda.requiredArgs.addAll(requiredArgs);
        invokeLambda.restArgs
                .addAll(generateArgExprs(pos, restArgs, Lists.of(invokableSymbol.restParam), symResolver));

        invokeLambda.symbol = invokableSymbol;
        invokeLambda.type = ((BInvokableType) invokableSymbol.type).retType;
        return invokeLambda;
    }

    static List<BLangSimpleVarRef> createVariableRefList(DiagnosticPos pos, List<BLangSimpleVariable> args) {
        final List<BLangSimpleVarRef> varRefs = new ArrayList<>();
        args.forEach(variable -> varRefs.add(createVariableRef(pos, variable.symbol)));
        return varRefs;
    }

    static BLangSimpleVarRef createVariableRef(DiagnosticPos pos, BSymbol varSymbol) {
        final BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.variableName = createIdentifier(pos, varSymbol.name.value);
        varRef.symbol = varSymbol;
        varRef.type = varSymbol.type;
        return varRef;
    }

    static BLangSimpleVarRef createIgnoreVariableRef(DiagnosticPos pos, SymbolTable symTable) {
        final BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.variableName = createIdentifier(pos, Names.IGNORE.value);
        varRef.symbol = new BVarSymbol(0, Names.IGNORE, symTable.rootPkgSymbol.scope.owner.pkgID, symTable.noType,
                symTable.rootPkgSymbol.scope.owner);
        varRef.type = symTable.noType;
        return varRef;
    }

    static BLangSimpleVariable createVariable(DiagnosticPos pos,
                                              String name,
                                              BType type,
                                              BLangExpression expr,
                                              BVarSymbol varSymbol) {
        final BLangSimpleVariable varNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        varNode.pos = pos;
        varNode.name = createIdentifier(pos, name);
        varNode.type = type;
        varNode.expr = expr;
        varNode.symbol = varSymbol;
        return varNode;
    }

    public static BLangSimpleVariable createVariable(DiagnosticPos pos, String name, BType type) {
        return createVariable(pos, name, type, null, null);
    }

    public static BLangSimpleVariableDef createVariableDef(DiagnosticPos pos, BLangSimpleVariable variable) {
        final BLangSimpleVariableDef variableDef =
                (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangTupleVariableDef createTupleVariableDef(DiagnosticPos pos, BLangTupleVariable variable) {
        final BLangTupleVariableDef variableDef =
                (BLangTupleVariableDef) TreeBuilder.createTupleVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangRecordVariableDef createRecordVariableDef(DiagnosticPos pos, BLangRecordVariable variable) {
        final BLangRecordVariableDef variableDef =
                (BLangRecordVariableDef) TreeBuilder.createRecordVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangErrorVariableDef createErrorVariableDef(DiagnosticPos pos, BLangErrorVariable variable) {
        final BLangErrorVariableDef variableDef =
                (BLangErrorVariableDef) TreeBuilder.createErrorVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.errorVariable = variable;
        return variableDef;
    }

    static BLangCheckedExpr createCheckExpr(DiagnosticPos pos, BLangExpression expr, BType returnType) {
        final BLangCheckedExpr checkExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkExpr.pos = pos;
        checkExpr.expr = expr;
        checkExpr.type = returnType;
        checkExpr.equivalentErrorTypeList = new ArrayList<>();
        return checkExpr;
    }

    static BLangBinaryExpr createBinaryExpr(DiagnosticPos pos,
                                            BLangExpression lhsExpr,
                                            BLangExpression rhsExpr,
                                            BType type,
                                            OperatorKind opKind,
                                            BOperatorSymbol symbol) {
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.lhsExpr = lhsExpr;
        binaryExpr.rhsExpr = rhsExpr;
        binaryExpr.type = type;
        binaryExpr.opKind = opKind;
        binaryExpr.opSymbol = symbol;
        return binaryExpr;
    }

    static BLangIsAssignableExpr createIsAssignableExpr(DiagnosticPos pos,
                                                        BLangExpression lhsExpr,
                                                        BType targetType,
                                                        BType type,
                                                        Names names) {
        final BLangIsAssignableExpr assignableExpr = new BLangIsAssignableExpr();
        assignableExpr.pos = pos;
        assignableExpr.lhsExpr = lhsExpr;
        assignableExpr.targetType = targetType;
        assignableExpr.type = type;
        assignableExpr.opSymbol = new BOperatorSymbol(names.fromString(assignableExpr.opKind.value()),
                null, targetType, null, InstructionCodes.IS_ASSIGNABLE);
        return assignableExpr;
    }

    static BLangIsLikeExpr createIsLikeExpr(DiagnosticPos pos, BLangExpression expr, BLangType typeNode,
                                            BType retType) {
        BLangIsLikeExpr isLikeExpr = (BLangIsLikeExpr) TreeBuilder.createIsLikeExpressionNode();
        isLikeExpr.pos = pos;
        isLikeExpr.expr = expr;
        isLikeExpr.typeNode = typeNode;
        isLikeExpr.type = retType;
        return isLikeExpr;
    }

    static BLangLiteral createLiteral(DiagnosticPos pos, BType type, Object value) {
        final BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.pos = pos;
        literal.value = value;
        literal.type = type;
        return literal;
    }

    static BLangRecordLiteral createEmptyRecordLiteral(DiagnosticPos pos, BType type) {
        final BLangRecordLiteral recordLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        recordLiteralNode.pos = pos;
        recordLiteralNode.type = type;
        return recordLiteralNode;
    }

    static BLangRecordLiteral.BLangRecordKeyValue createBLangRecordKeyValue(BLangExpression key,
                                                                            BLangExpression value) {
        final BLangRecordLiteral.BLangRecordKeyValue recordKeyValue =
                (BLangRecordLiteral.BLangRecordKeyValue) TreeBuilder.createRecordKeyValue();
        recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(key);
        recordKeyValue.valueExpr = value;
        return recordKeyValue;
    }

    static BLangListConstructorExpr.BLangArrayLiteral createEmptyArrayLiteral(DiagnosticPos pos, BArrayType type) {
        final BLangListConstructorExpr.BLangArrayLiteral arrayLiteralNode =
                (BLangListConstructorExpr.BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        arrayLiteralNode.pos = pos;
        arrayLiteralNode.type = type;
        arrayLiteralNode.exprs = new ArrayList<>();
        return arrayLiteralNode;
    }

    static BLangTypeInit createEmptyTypeInit(DiagnosticPos pos, BType type) {
        BLangTypeInit objectInitNode = (BLangTypeInit) TreeBuilder.createInitNode();
        objectInitNode.pos = pos;
        objectInitNode.type = type;

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.symbol = ((BObjectTypeSymbol) type.tsymbol).initializerFunc.symbol;
        invocationNode.type = type;

        BLangIdentifier pkgNameNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        BLangIdentifier nameNode = (BLangIdentifier)  TreeBuilder.createIdentifierNode();

        nameNode.setLiteral(false);
        nameNode.setValue(Names.OBJECT_INIT_SUFFIX.getValue());
        invocationNode.name = nameNode;
        invocationNode.pkgAlias = pkgNameNode;

        objectInitNode.initInvocation = invocationNode;
        return objectInitNode;
    }

    static BLangTableLiteral createEmptyTableLiteral(DiagnosticPos pos, BType type, BType configType) {
        final BLangTableLiteral tableLiteralNode = (BLangTableLiteral) TreeBuilder.createTableLiteralNode();
        tableLiteralNode.pos = pos;
        tableLiteralNode.type = type;
        return tableLiteralNode;
    }

    public static BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        final BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        node.pos = pos;
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    public static BLangStatementExpression createStatementExpression(BLangStatement stmt, BLangExpression expr) {
        BLangStatementExpression stmtExpr = (BLangStatementExpression) TreeBuilder.creatStatementExpression();
        stmtExpr.stmt = stmt;
        stmtExpr.expr = expr;
        stmtExpr.pos = stmt.pos;
        return stmtExpr;
    }

    public static BLangMatchExpression createMatchExpression(BLangExpression expr) {
        BLangMatchExpression matchExpr = (BLangMatchExpression) TreeBuilder.createMatchExpression();
        matchExpr.expr = expr;
        return matchExpr;
    }

    public static BLangFieldBasedAccess createFieldAccessExpr(BLangVariableReference varRef, BLangIdentifier field) {
        return createFieldAccessExpr(varRef, field, false);
    }

    public static BLangFieldBasedAccess createFieldAccessExpr(BLangVariableReference varRef, BLangIdentifier field,
                                                              boolean except) {
        BLangFieldBasedAccess fieldAccessExpr = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldAccessExpr.expr = varRef;
        fieldAccessExpr.field = field;
        fieldAccessExpr.except = except;
        return fieldAccessExpr;
    }

    public static BLangIndexBasedAccess createIndexAccessExpr(BLangVariableReference varRef,
                                                              BLangExpression indexExpr) {
        BLangIndexBasedAccess fieldAccessExpr = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        fieldAccessExpr.expr = varRef;
        fieldAccessExpr.indexExpr = indexExpr;
        return fieldAccessExpr;
    }

    public static BLangFunction createInitFunction(DiagnosticPos pos, String name, Name sufix) {
        BLangFunction initFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        initFunction.setName(createIdentifier(name + sufix.getValue()));
        initFunction.flagSet = EnumSet.of(Flag.PUBLIC);
        initFunction.pos = pos;

        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = TypeKind.NIL;
        initFunction.returnTypeNode = typeNode;

        // Create body of the init function
        BLangBlockStmt body = (BLangBlockStmt) TreeBuilder.createBlockNode();
        body.pos = pos;
        initFunction.setBody(body);
        return initFunction;
    }

    static BLangServiceConstructorExpr createServiceConstructor(BLangService service) {
        BLangServiceConstructorExpr constExpr = (BLangServiceConstructorExpr) TreeBuilder
                .createServiceConstructorNode();
        constExpr.pos = service.pos;
        constExpr.serviceNode = service;
        constExpr.type = service.symbol.type;
        return constExpr;
    }

    public static BLangSimpleVariable createReceiver(DiagnosticPos pos, BType type) {
        BLangSimpleVariable receiver = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        receiver.pos = pos;
        IdentifierNode identifier = createIdentifier(Names.SELF.getValue());
        receiver.setName(identifier);
        receiver.type = type;
        return receiver;
    }

    public static BLangNamedArgsExpression createNamedArg(String argName, BLangExpression expr) {
        BLangNamedArgsExpression argExpr = new BLangNamedArgsExpression();
        argExpr.name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        argExpr.name.value = argName;
        argExpr.expr = expr;
        return argExpr;
    }

    public static BVarSymbol duplicateVarSymbol(BVarSymbol varSymbol) {
        BVarSymbol dupVarSymbol = new BVarSymbol(varSymbol.flags, varSymbol.name,
                varSymbol.pkgID, varSymbol.type, varSymbol.owner);
        dupVarSymbol.varIndex = varSymbol.varIndex;
        dupVarSymbol.tainted = varSymbol.tainted;
        dupVarSymbol.closure = varSymbol.closure;
        dupVarSymbol.markdownDocumentation = varSymbol.markdownDocumentation;
        dupVarSymbol.scope = varSymbol.scope;
        dupVarSymbol.type = varSymbol.type;
        dupVarSymbol.kind = varSymbol.kind;

        return dupVarSymbol;
    }

    private static IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    public static BInvokableSymbol duplicateInvokableSymbol(BInvokableSymbol invokableSymbol) {
        BInvokableSymbol dupFuncSymbol = Symbols.createFunctionSymbol(invokableSymbol.flags, invokableSymbol.name,
                invokableSymbol.pkgID, invokableSymbol.type, invokableSymbol.owner, invokableSymbol.bodyExist);
        dupFuncSymbol.receiverSymbol = invokableSymbol.receiverSymbol;
        dupFuncSymbol.retType = invokableSymbol.retType;
        dupFuncSymbol.restParam = invokableSymbol.restParam;
        dupFuncSymbol.params = new ArrayList<>(invokableSymbol.params);
        dupFuncSymbol.taintTable = invokableSymbol.taintTable;
        dupFuncSymbol.tainted = invokableSymbol.tainted;
        dupFuncSymbol.closure = invokableSymbol.closure;
        dupFuncSymbol.markdownDocumentation = invokableSymbol.markdownDocumentation;
        dupFuncSymbol.scope = invokableSymbol.scope;
        dupFuncSymbol.tag = invokableSymbol.tag;

        BInvokableType prevFuncType = (BInvokableType) invokableSymbol.type;
        dupFuncSymbol.type = new BInvokableType(new ArrayList<>(prevFuncType.paramTypes),
                prevFuncType.retType, prevFuncType.tsymbol);
        return dupFuncSymbol;
    }

    public static BInvokableSymbol duplicateInvokableSymbol(BInvokableSymbol invokableSymbol, BSymbol owner,
                                                            Name newName, PackageID newPkgID) {
        BInvokableSymbol dupFuncSymbol = Symbols.createFunctionSymbol(invokableSymbol.flags, newName, newPkgID,
                invokableSymbol.type, owner, invokableSymbol.bodyExist);
        dupFuncSymbol.receiverSymbol = invokableSymbol.receiverSymbol;
        dupFuncSymbol.retType = invokableSymbol.retType;

        dupFuncSymbol.params = invokableSymbol.params.stream()
                .map(param -> duplicateParamSymbol(param, dupFuncSymbol))
                .collect(Collectors.toList());
        if (dupFuncSymbol.restParam != null) {
            dupFuncSymbol.restParam = duplicateParamSymbol(invokableSymbol.restParam, dupFuncSymbol);
        }

        dupFuncSymbol.taintTable = invokableSymbol.taintTable;
        dupFuncSymbol.tainted = invokableSymbol.tainted;
        dupFuncSymbol.closure = invokableSymbol.closure;
        dupFuncSymbol.tag = invokableSymbol.tag;
        dupFuncSymbol.markdownDocumentation = invokableSymbol.markdownDocumentation;

        BInvokableType prevFuncType = (BInvokableType) invokableSymbol.type;
        dupFuncSymbol.type = new BInvokableType(new ArrayList<>(prevFuncType.paramTypes), prevFuncType.retType,
                prevFuncType.tsymbol);
        return dupFuncSymbol;
    }

    private static BVarSymbol duplicateParamSymbol(BVarSymbol paramSymbol, BInvokableSymbol owner) {
        BVarSymbol newParamSymbol =
                new BVarSymbol(paramSymbol.flags, paramSymbol.name, paramSymbol.pkgID, paramSymbol.type, owner);
        newParamSymbol.tainted = paramSymbol.tainted;
        newParamSymbol.defaultableParam = paramSymbol.defaultableParam;
        newParamSymbol.markdownDocumentation = paramSymbol.markdownDocumentation;
        return newParamSymbol;
    }

    static BLangInvocation createLambdaInvocation(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                  BLangSimpleVarRef varRef, List<BLangSimpleVariable> requiredArgs,
                                                  SymbolResolver symResolver) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        BLangIdentifier invocationName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        invocationName.setValue(BLangBuiltInMethod.CALL.getName());
        invokeLambda.name = invocationName;
        invokeLambda.argExprs.addAll(generateArgExprsForLambdas(pos, requiredArgs, invokableSymbol.params,
                symResolver));
        invokeLambda.requiredArgs.addAll(generateArgExprsForLambdas(pos, requiredArgs, invokableSymbol.params,
                symResolver));
        invokeLambda.builtInMethod = BLangBuiltInMethod.CALL;
        invokeLambda.type = ((BInvokableType) invokableSymbol.type).retType;
        invokeLambda.expr = varRef;
        invokeLambda.builtinMethodInvocation = true;
        invokeLambda.symbol = varRef.symbol;
        return invokeLambda;
    }

    private static List<BLangExpression> generateArgExprsForLambdas(DiagnosticPos pos, List<BLangSimpleVariable> args,
                                                                    List<BVarSymbol> formalParams,
                                                                    SymbolResolver symResolver) {
        List<BLangExpression> argsExpr = new ArrayList<>();
        final List<BLangSimpleVarRef> variableRefList = createVariableRefList(pos, args);
        int mapSymbolsParams = formalParams.size() - args.size();
        for (int i = 0; i < variableRefList.size(); i++) {
            BLangSimpleVarRef varRef = variableRefList.get(i);
            BType target = formalParams.get(i + mapSymbolsParams).type;
            BType source = varRef.symbol.type;
            if (source != target) {
                argsExpr.add(generateConversionExpr(varRef, target, symResolver));
                continue;
            }
            argsExpr.add(varRef);
        }
        return argsExpr;
    }

    static BLangXMLTextLiteral createXMLTextLiteralNode(BLangBinaryExpr parent, BLangExpression concatExpr,
                                                        DiagnosticPos pos, BType type) {
        BLangXMLTextLiteral xmlTextLiteral = new BLangXMLTextLiteral();
        xmlTextLiteral.concatExpr = concatExpr;
        xmlTextLiteral.pos = pos;
        xmlTextLiteral.parent = parent;
        xmlTextLiteral.type = type;
        return xmlTextLiteral;
    }
}
