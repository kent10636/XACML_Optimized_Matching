import pickle as p


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
    res['Deny'] = pDeny
    res['Permit'] = pPermit
    print(pDeny, pPermit)
    return max(res, key=res.get)


if __name__ == '__main__':
    file = open('train_result.pkl', 'rb')
    probMat = p.load(file)
    # Deny,test,COMMENT,UPDATE,SALEOPENEDMINOR
    # Permit,newSub3,USERACCOUNT,ATTENDEEACTIVITY,WORKINGDAY
    res = test(['test', 'COMMENT', 'SETMEETINGMODERATOR', 'MAINTENANCEDAY'], probMat)
    file.close()
    print(res)
