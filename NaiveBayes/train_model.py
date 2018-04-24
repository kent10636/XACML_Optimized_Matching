import pickle


def train(dataSet, labels):
    uniqueLabels = set(labels)
    res = {}
    for label in uniqueLabels:
        res[label] = []
        res[label].append(labels.count(label) / float(len(labels)))
        for i in range(len(dataSet[0]) - 1):
            tempCols = [l[i] for l in dataSet if l[0] == label]  # 获取Ai的值
            uniqueValues = set(tempCols)
            dict = {}
            for value in uniqueValues:
                count = tempCols.count(value)
                prob = count / float(labels.count(label))  # 计算P(A|B)
                dict[value] = prob
            res[label].append(dict)

    file = open('train_result.pkl', 'wb')
    pickle.dump(res, file)
    file.close()

    print(res)
    return res


def loadDataSet(filename):
    fr = open(filename)
    arrayOLines = fr.readlines()
    returnMat = []
    labels = []
    for line in arrayOLines:
        line = line.strip()
        listFromLine = line.split(',')
        labels.append(listFromLine[0])
        returnMat.append(listFromLine[0:5])

        '''for i in labels:
            print(i)'''
        '''for i in returnMat:
            print(i)'''

    return returnMat, labels

def test(testVect, probMat):
    Deny = probMat['Deny']
    Permit = probMat['Permit']
    pDeny = Deny[0]
    pPermit = Permit[0]
    for i in range(len(testVect)):
        if testVect[i] in Deny[i + 1]:
            pDeny *= Deny[i + 1][testVect[i]]
        else:
            pDeny = 0

        if testVect[i] in Permit[i + 1]:
            pPermit *= Permit[i + 1][testVect[i]]
        else:
            pPermit = 0
    probMat['Deny'] = pDeny
    probMat['Permit'] = pPermit
    print(pDeny, pPermit)
    return max(probMat, key=probMat.get)

if __name__ == '__main__':
    filename = 'extraction.csv'
    dataSet, labels = loadDataSet(filename)
    # train(dataSet, labels)

    probMat = train(dataSet, labels)
    res = test(['test', 'COMMENT', 'SETMEETINGMODERATOR', 'MAINTENANCEDAY'], probMat)
    print(res)