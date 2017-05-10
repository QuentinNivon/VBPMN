import xml.etree.ElementTree
import pif


class PifTraversal:
    def findNodeById(self, proc, id):
        target = None
        for flow in proc.behaviour.sequenceFlows:
            if flow.id == id:
                target = flow.target
        for n in proc.behaviour.nodes:
            if n.id == target:
                return n
        return None

    def traverseNode(self, proc, node, visited, stack):

        if node is None:
            return True
        if node not in visited:
            visited.append(node)
            seqFlows = node.outgoingFlows

            if not seqFlows:
                if not stack:
                    return True
                else:
                    return False
            for flow in seqFlows:
                targetId = flow
                targetNode = self.findNodeById(proc, targetId)
                if isinstance(targetNode, pif.OrSplitGateway_):
                    stack.append(targetNode)
                if isinstance(targetNode, pif.OrJoinGateway_):
                    sourceSplit = stack.pop()
                    if isinstance(sourceSplit, pif.OrSplitGateway_):
                        if len(targetNode.incomingFlows) != len(sourceSplit.outgoingFlows):
                            return False
                return self.traverseNode(proc, targetNode, visited, stack)
        else:
            # loop found
            return False
            # return True


if __name__ == '__main__':
    # set up parser
    import argparse

    parser = argparse.ArgumentParser(prog='PifCheck', description='Checks pif models for unbalanced inclusive gateways')
    parser.add_argument('--version', action='version', version='%(prog)s 1.0')
    parser.add_argument('filename')
    args = parser.parse_args()
    result = False
    initialNode = None
    with open(args.filename) as f:
        xml = f.read()
        proc = pif.CreateFromDocument(xml)
        for n in proc.behaviour.nodes:
            if isinstance(n, pif.InitialEvent_):
                initialNode = n
        visited = []
        stack = []
        pifTraversal = PifTraversal()
        result = pifTraversal.traverseNode(proc, initialNode, visited, stack)
        if result:
            print "TRUE: PIF is balanced"
        else:
            print "FALSE: PIF is unbalanced"

def checkInclusiveUnbalanced(file1 , file2):
    result1 = checkPifFile(file1)
    result2 = checkPifFile(file2)

    return result1 and result2


def checkPifFile(pifFile):
    result = False
    initialNode = None
    with open(pifFile) as f:
        xml = f.read()
        proc = pif.CreateFromDocument(xml)
        for n in proc.behaviour.nodes:
            if isinstance(n, pif.InitialEvent_):
                initialNode = n
        visited = []
        stack = []
        pifTraversal = PifTraversal()
        result = pifTraversal.traverseNode(proc, initialNode, visited, stack)
        return result