class UserAgora {
  String? name;
  String? avatar;
  String? userUuid;
  String? token;
  bool? hasPhone;
  bool? hasPassword;

  UserAgora({
    this.name,
    this.avatar,
    this.userUuid,
    this.token,
    this.hasPhone,
    this.hasPassword,
  });

  factory UserAgora.fromJson(Map<String, dynamic> json) => UserAgora(
        name: json['name'] as String?,
        avatar: json['avatar'] as String?,
        userUuid: json['userUUID'] as String?,
        token: json['token'] as String?,
        hasPhone: json['hasPhone'] as bool?,
        hasPassword: json['hasPassword'] as bool?,
      );

  Map<String, dynamic> toJson() => {
        'name': name,
        'avatar': avatar,
        'userUUID': userUuid,
        'token': token,
        'hasPhone': hasPhone,
        'hasPassword': hasPassword,
      };
}
