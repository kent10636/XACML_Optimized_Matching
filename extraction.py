import xml.sax

file = open('extraction.csv', 'w')


class A:
    i = 0


# file.write('Effect' + ',' + 'RuleId' + ',' + 'Subject' + ',' + 'Resource' + ',' + 'Action' + ',' + 'Condition')

class XACMLHandler(xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.Effect = ""
        self.RuleId = ""
        self.Subject = ""
        self.Resource = ""
        self.Action = ""
        self.Condition = ""
        self.AttributeValue = ""

    def startElement(self, tag, attributes):  # 元素开始事件处理
        self.CurrentData = tag
        if tag == "Rule":
            RuleId = attributes["RuleId"]
            print("RuleId:", RuleId)
            Effect = attributes["Effect"]
            print("Effect:", Effect)
            # file.write('\n' + Effect + ',')
            file.write(Effect + ',')

    def endElement(self, tag):
        if self.CurrentData == "Subject":
            print("Subject:", self.Subject)
        elif self.CurrentData == "Resource":
            print("Resource:", self.Resource)
        elif self.CurrentData == "Action":
            print("Action:", self.Action)
        elif self.CurrentData == "Condition":
            print("Condition:", self.Condition)
        elif self.CurrentData == "AttributeValue":
            print("AttributeValue:", self.AttributeValue)
            file.write(self.AttributeValue + ',')

            A.i = A.i + 1
            if A.i == 4:
                file.write('\n')
                A.i = 0

            self.AttributeValue = ""
        self.CurrentData = ""

    def characters(self, content):  # 内容事件处理
        if self.CurrentData == "Subject":
            self.Subject = content
        elif self.CurrentData == "Resource":
            self.Resource = content
        elif self.CurrentData == "Action":
            self.Action = content
        elif self.CurrentData == "Condition":
            self.Condition = content
        elif self.CurrentData == "AttributeValue":
            self.AttributeValue = content


if (__name__ == "__main__"):
    # 创建一个 XMLReader
    parser = xml.sax.make_parser()

    # turn off namepsaces
    parser.setFeature(xml.sax.handler.feature_namespaces, 0)

    # 重写 ContextHandler
    Handler = XACMLHandler()
    parser.setContentHandler(Handler)

    parser.parse("lms.xml")
