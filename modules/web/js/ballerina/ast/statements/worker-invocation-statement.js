/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import _ from 'lodash';
import Statement from './statement';
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

/**
 * Class to represent a worker invocation statement in ballerina.
 * @constructor
 */
class WorkerInvocationStatement extends Statement {
    constructor(args) {
        super('WorkerInvocationStatement');
        this._source = _.get(args, 'source');
        this._destination = _.get(args, 'destination');
        this._expressionList = _.get(args, 'expressionList', []);
        this._invokeStatement = _.get(args, 'invokeStatement', 'messageName -> workerName');
        this._workerName = _.get(args, 'workerName', '');
    }

    setSource(source) {
        this._source = source;
    }

    getSource() {
        return this._source;
    }

    setDestination(destination) {
        this._destination = destination;
    }

    getDestination() {
        return this._destination;
    }

    setWorkerName(workerName) {
        this._workerName = workerName;
    }

    getWorkerName() {
        return this._workerName;
    }

    addToExpressionList(expression) {
        this._expressionList.push(expression);
    }

    getExpressionList() {
        return this._expressionList;
    }

    setInvocationStatement(invocationStatement) {
        this._invokeStatement = invocationStatement;
    }

    getInvocationStatement() {
        return this._invokeStatement;
    }

    canBeAChildOf(node) {
        return this.getFactory().isResourceDefinition(node)
            || this.getFactory().isFunctionDefinition(node)
            || this.getFactory().isConnectorAction(node)
            || (this.getFactory().isStatement(node) && !node._isChildOfWorker);
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var workerName = jsonNode.worker_name;const self = this;
        const expressionList = jsonNode.expression_list;
        let expressionString = '';

        for (let itr = 0; itr < expressionList.length; itr ++) {
            const expressionNode = BallerinaASTFactory.createFromJson(expressionList[itr].expression[0]);
            expressionNode.initFromJson(expressionList[itr].expression[0]);
            self.addToExpressionList(expressionNode);
            expressionString += expressionNode.getExpression();
            if (itr !== expressionList.length - 1) {
                expressionString += ',';
            }
        }

        var invokeStatement = expressionString + ' -> ' + workerName;
        this.setInvocationStatement(invokeStatement);
        var workerInstance = _.find(this.getParent().getChildren(), function (child) {
            return self.getFactory().isWorkerDeclaration(child) && !child.isDefaultWorker() && child.getWorkerName() === workerName;
        });
        this.setDestination(workerInstance);
        this.setWorkerName(workerName);
    }

    messageDrawTargetAllowed(target) {
        return this.getFactory().isWorkerDeclaration(target);
    }
}

export default WorkerInvocationStatement;

