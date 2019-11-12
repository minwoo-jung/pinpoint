import * as go from 'gojs';

import ServerMapTheme from './server-map-theme';
import { ServerMapTemplateWithGojs } from './server-map-template-with-gojs.class';
import { ServerMapDiagram } from './server-map-diagram.class';
import { ServerMapData } from './server-map-data.class';
import { IServerMapOption } from './server-map-factory';
import { ServerMapNodeClickExtraParam } from './server-map-node-click-extra-param.class';

export class ServerMapDiagramWithGojs extends ServerMapDiagram {
    private diagram: go.Diagram = null;
    private groupServiceTypeList: string[];

    constructor(
        private option: IServerMapOption
    ) {
        super();
        ServerMapTheme.general.common.funcServerMapImagePath = this.option.funcServerMapImagePath;
        this.initLicense();
        this.makeDiagram();
        this.setNodeDefaultTemplate();
        this.setLinkTemplate();
        this.setDiagramEnvironment();
        this.setEvent();
    }
    private initLicense(): void {
        (go as any).licenseKey = '73fd42eab51c28c702d90776423d6bf919a57863c6851fa30a0715f6e9086d1c259dea2a55d7d9c680f846ee0a7ac889dbc56879911b0039e130dbd543e584f0b63327b510084788f70172909dff7af5ff7f72f5c2bd76f7d36a9cf4bef8c59c0eb8f2c658c90fbb2167032e5e';
    }
    makeDiagram(): void {
        this.diagram = go.GraphObject.make(go.Diagram, this.option.container, {
            allowDelete: false,
            maxSelectionCount: 1,
            initialContentAlignment: go.Spot.Center
        });
        this.diagram.animationManager.isEnabled = false;
        this.diagram.scrollMode = go.Diagram.InfiniteScroll;
    }
    setNodeDefaultTemplate(): void {
        this.diagram.nodeTemplate = ServerMapTemplateWithGojs.makeNodeTemplate(this);
    }
    setNodeTemplateMap(): void {
        this.groupServiceTypeList.forEach((groupType: string) => {
            this.diagram.nodeTemplateMap.add(groupType, ServerMapTemplateWithGojs.makeNodeGroupTemplate(this));
        });
    }
    setLinkTemplate(): void {
        this.diagram.linkTemplate = ServerMapTemplateWithGojs.makeLinkTemplate(this);
    }
    setDiagramEnvironment(): void {
        const $ = go.GraphObject.make;

        this.diagram.toolManager.mouseWheelBehavior = go.ToolManager.WheelZoom;
        this.diagram.allowDrop = false;

        this.diagram.initialAutoScale = go.Diagram.Uniform;
        this.diagram.toolManager.draggingTool.doCancel();
        this.diagram.toolManager.draggingTool.doDeactivate();
        this.diagram.toolManager.dragSelectingTool.isEnabled = false;
        this.diagram.initialContentAlignment = go.Spot.Center;
        this.diagram.padding = new go.Margin(10, 10, 10, 10);
        this.diagram.layout = $(
            go.LayeredDigraphLayout,
            {
                isOngoing: false,
                layerSpacing: 100,
                columnSpacing: 30,
                setsPortSpots: false
            }
        );
    }
    setEvent(): void {
        const self = this;
        this.diagram.addDiagramListener('InitialLayoutCompleted', (event: go.DiagramEvent) => {
            if (self.serverMapData) {
                self.outRenderCompleted.emit(event.diagram);
            }
        });
        this.diagram.addDiagramListener('BackgroundSingleClicked', () => {
            self.outClickBackground.emit();
        });
        this.diagram.addDiagramListener('BackgroundDoubleClicked', (event: go.DiagramEvent) => {
            event.diagram.zoomToFit();
            self.outDoubleClickBackground.emit('dbclickBackground');
        });
        // this.diagram.addDiagramListener('BackgroundContextClicked', (event: go.DiagramEvent) => {
        //     console.log('Background context click', event);
        //     self.outContextClickBackground.emit({
        //         event: event
        //     });
        // });
        this.diagram.addDiagramListener('BackgroundContextClicked', (event: go.DiagramEvent) => {
            const { pageX, pageY } = event.diagram.lastInput.event as MouseEvent;
            
            self.outContextClickBackground.emit({
                coordX: pageX,
                coordY: pageY
            });
        });
    }
    setMapData(serverMapData: ServerMapData, baseApplicationKey = '') {
        this.serverMapData = serverMapData;
        this.groupServiceTypeList = serverMapData.getGroupTypes();
        this.baseApplicationKey = baseApplicationKey;
        this.setNodeTemplateMap();

        this.diagram.model = go.Model.fromJson({
            nodeDataArray: this.serverMapData.getNodeList(),
            linkDataArray: this.serverMapData.getLinkList()
        });
        // this.diagram.undoManager.isEnabled = true;
        this.selectBaseApplication();
    }
    private selectBaseApplication() {
        if (this.baseApplicationKey !== '') {
            let node = this.diagram.findPartForKey(this.baseApplicationKey);
            if (node === null) {
                node = this.checkAuthorizedNode();
            }
            if (node) {
                this.diagram.select(node);
                this.onClickNodeManually(node);
            }
        }
    }
    private checkAuthorizedNode(): go.Part {
        return this.diagram.findPartForKey(this.baseApplicationKey.replace(/(.*)\^(.*)/i, '$1^UNAUTHORIZED'));
    }
    private onClickNodeManually(obj: go.Part): void {
        this.updateHighlights(obj);
        this.outClickNode.emit(<go.Node>obj['data']);
    }
    private updateHighlights(selection: go.Part): void {
        this.removeHighlightMark();
        selection['highlight'] = 'self';
        if (selection instanceof go.Node) {
        this.addHighlightMarkToLink(selection);
        } else if (selection instanceof go.Link) {
        this.addHighlightMarkToNode(selection);
        }
        this.drawHighlight();
    }
    private removeHighlightMark(): void {
        const allNodes = this.diagram.nodes;
        const allLinks = this.diagram.links;
        while (allNodes.next()) {
            delete allNodes.value['highlight'];
        }
        while (allLinks.next()) {
            delete allLinks.value['highlight'];
        }
    }
    private addHighlightMarkToLink(selection: go.Part) {
        const intoLinks = (<go.Node>selection).findLinksInto();
        while (intoLinks.next()) {
            intoLinks.value['highlight'] = 'from';
        }
        const outofLinks = (<go.Node>selection).findLinksOutOf();
        while (outofLinks.next()) {
            outofLinks.value['highlight'] = 'to';
        }
    }
    private addHighlightMarkToNode(selection: go.Part) {
        (<go.Link>selection).fromNode['highlight'] = 'from';
        (<go.Link>selection).toNode['highlight'] = 'to';
    }
    private drawHighlight(): void {
        const allNodes = this.diagram.nodes;
        const allLinks = this.diagram.links;

        while (allNodes.next()) {
        this.highlightNode(<go.Node>allNodes.value);
        }
        while (allLinks.next()) {
            this.highlightLink(<go.Link>allLinks.value);
        }
    }
    private highlightNode(targetNode: go.Node): void {
        const shape: go.Shape = <go.Shape>targetNode.findObject('BORDER_SHAPE');
        const nodeStyle = targetNode['highlight'] ? ServerMapTheme.general.node.highlight : ServerMapTheme.general.node.normal;

        shape['stroke'] = nodeStyle.border.stroke;
        shape['strokeWidth'] = nodeStyle.border.strokeWidth;
        shape.part.isShadowed = false;
    }
    private highlightLink(selectedLink: go.Link, theme?: any, toFill?: any): void {
        const line: go.Shape = <go.Shape>selectedLink.findObject('LINK');
        const arrow: go.Shape = <go.Shape>selectedLink.findObject('ARROW');
        const text: go.TextBlock = <go.TextBlock>selectedLink.findObject('LINK_TEXT');
        const linkStyle = selectedLink['highlight'] ? ServerMapTheme.general.link.highlight : ServerMapTheme.general.link.normal;

        line['stroke'] = linkStyle.line.stroke;
        arrow['stroke'] = linkStyle.arrow.stroke;
        arrow['fill'] = linkStyle.arrow.fill;
        text['font'] = linkStyle.fontFamily;
    }
    isBaseApplication(key: string): boolean {
        return this.baseApplicationKey === key;
    }
    selectNodeBySearch(highlightApplicationKey: string): void {
        const node: go.Node = this.searchHighlightNode(highlightApplicationKey);
        this.diagram.select(node);
        this.diagram.centerRect(node.actualBounds);
        this.updateHighlights(<go.Part>node);
        this.outClickNode.emit(node['data']);
    }
    private searchHighlightNode(highlightApplicationKey: string): go.Node {
        const allNodes = this.diagram.nodes;
        let resultNode: go.Node;

        while (allNodes.next()) {
            const node: go.Node = allNodes.value;
            if (node.data.mergedNodes) {
                const mergedNodes = node.data.mergedNodes;
                for (let i = 0; i < mergedNodes.length ; i++ ) {
                    if (mergedNodes[i].key === highlightApplicationKey) {
                        resultNode = node;
                        break;
                    }
                }
            } else {
                if (node.data.key === highlightApplicationKey) {
                    resultNode = node;
                    break;
                }
            }
        }
        return resultNode;
    }
    refresh(): void {
        this.diagram.model = go.Model.fromJson({
            nodeDataArray: this.serverMapData.getNodeList(),
            linkDataArray: this.serverMapData.getLinkList()
        });
        this.diagram.rebuildParts();
        this.selectBaseApplication();
    }
    clear(): void {
        this.diagram.model = go.Model.fromJson({});
    }
    onClickNode(event: go.InputEvent, obj: go.GraphObject): void {
        const part = obj.part ? obj.part : <go.Part>obj;

        this.updateHighlights(part);
        this.outClickNode.emit(part['data']);
    }
    onDoubleClickNode(event: go.InputEvent, obj: go.GraphObject): void {
        // console.log('onDoubleClick-Node :', event, obj);
        this.diagram.centerRect(obj.actualBounds);
        this.diagram.scale *= 2;
    }
    onContextClickNode(event: go.InputEvent, obj: go.GraphObject): void {
        // console.log('onContextClick-Node :', event, obj);
        this.outContextClickNode.emit(<go.Node>obj);
    }
    onClickLink(event: go.InputEvent, obj: go.GraphObject): void {
        // console.log('onClick-Link :', event, obj);
        this.updateHighlights(<go.Part>obj);
        this.outClickLink.emit(<go.Link>obj['data']);
    }
    onContextClickLink(event: any, obj: go.GraphObject): void {
        const { key, targetInfo } = (obj as go.Link).data;
        const { pageX, pageY } = event.event;

        if (!Array.isArray(targetInfo)) {
            this.outContextClickLink.emit({
                key,
                coord: {
                    coordX: pageX,
                    coordY: pageY
                }
            });
        }
    }
}
