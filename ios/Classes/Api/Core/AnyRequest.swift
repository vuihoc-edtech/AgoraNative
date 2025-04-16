//
//  AnyRequest.swift
//  Flat
//
//  Created by xuyunshi on 2021/11/9.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation

struct AnyRequest<Response: Decodable>: Request {
    var responseType: Response.Type
    var path: String
    var method: HttpMethod
    var task: Task
    var decoder: JSONDecoder

    init<T: Request>(request: T) where T.Response == Response {
        path = request.path
        responseType = request.responseType
        method = request.method
        task = request.task
        decoder = request.decoder
    }
}
