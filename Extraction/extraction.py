# 功能：提取XACML中规则(Rule)的5个关键属性Effect、Subject、Resourse、Action、Condition
# 算法：利用Python爬虫常用的BeautifulSoup像解析HTML一样解析XML，读取其对应标签的关键信息

from bs4 import BeautifulSoup  # 导入BeautifulSoup 模块

xml_path = 'E:\XACML_Optimized_Matching\Extraction\\'  # XML文件存放路径
file_path = 'E:\XACML_Optimized_Matching\Extraction\\'  # 提取后文件存放路径

file = open(file_path + 'extraction.csv', 'w')  # 提取文件名extraction.csv


def extract():
    soup = BeautifulSoup(open(xml_path + 'asms.xml'), "lxml")  # 声明BeautifulSoup对象，说明将要提取属性的XACML策略集名称
    # print(soup.prettify())

    # print(soup.rule.get('effect'))
    # print(soup.rule.subject.attributevalue.string)
    # print(soup.rule.resource.attributevalue.string)
    # print(soup.rule.action.attributevalue.string)
    # print(soup.rule.condition.attributevalue.string)

    num = 0
    rule = soup.find_all('rule')
    for ru in rule:
        ef = ru.get('effect')
        file.write(ef + ',')  # effect写入到文件

        su = ru.subject.attributevalue.string
        if (su):  # 非空，则不变
            su
        else:  # 空，则赋值为空串
            su = ''
        file.write(su + ',')  # subject写入到文件

        re = ru.resource.attributevalue.string
        if (re):  # 非空，则不变
            re
        else:  # 空，则赋值为空串
            re = ''
        file.write(re + ',')  # resource写入到文件

        ac = ru.action.attributevalue.string
        if (ac):  # 非空，则不变
            ac
        else:  # 空，则赋值为空串
            ac = ''
        file.write(ac + ',')  # action写入到文件

        co = ru.condition.attributevalue.string
        if (co):  # 非空，则不变
            co
        else:  # 空，则赋值为空串
            co = ''
        file.write(co + ',')  # condition写入到文件

        file.write('\n')

        print(ef + ',' + su + ',' + re + ',' + ac + ',' + co)
        num = num + 1

    print(num)  # 打印测试一共遍历了多少条策略


extract()
